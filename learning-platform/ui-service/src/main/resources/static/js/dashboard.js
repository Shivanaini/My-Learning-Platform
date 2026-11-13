// ---------- Guard: must be logged in ----------
if (!Api.token()) {
    window.location.href = '/index.html';
}

const $ = id => document.getElementById(id);
const user = Api.user() || {};
const isInstructor = user.role === 'INSTRUCTOR';
let lectures = [];
let selectedFile = null;

const ICONS = { VIDEO: '🎬', AUDIO: '🎧', PDF: '📕', IMAGE: '🖼️', DOCUMENT: '📄', OTHER: '📦' };

// ---------- Helpers ----------
function escapeHtml(s) {
    return String(s ?? '').replace(/[&<>"']/g, c =>
        ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

function formatSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    if (bytes < 1024 * 1024 * 1024) return (bytes / 1024 / 1024).toFixed(1) + ' MB';
    return (bytes / 1024 / 1024 / 1024).toFixed(2) + ' GB';
}

function formatDate(iso) {
    return new Date(iso).toLocaleDateString(undefined, { day: 'numeric', month: 'short', year: 'numeric' });
}

function openModal(id) { $(id).classList.remove('hidden'); }

function closeModal(id) {
    $(id).classList.add('hidden');
    if (id === 'viewerModal') $('viewerContent').innerHTML = ''; // stops video playback
}

function debounce(fn, ms) {
    let t;
    return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), ms); };
}

// ---------- Header ----------
$('userName').textContent = user.fullName || user.username || '';
$('userRole').textContent = user.role || '';
if (user.role) $('userRole').classList.add(user.role);
$('avatar').textContent = (user.fullName || user.username || '?').charAt(0).toUpperCase();
$('logoutBtn').addEventListener('click', Api.logout);
if (isInstructor) $('openUploadBtn').classList.remove('hidden');

// ---------- Load & render lectures ----------
async function loadCourses() {
    const courses = await Api.get('/api/lectures/courses') || [];
    const current = $('courseFilter').value;
    $('courseFilter').innerHTML = '<option value="">All courses</option>' +
        courses.map(c => `<option value="${escapeHtml(c)}">${escapeHtml(c)}</option>`).join('');
    $('courseFilter').value = courses.includes(current) ? current : '';
    $('courseList').innerHTML = courses.map(c => `<option value="${escapeHtml(c)}">`).join('');
}

async function loadLectures() {
    $('pageError').classList.add('hidden');
    const params = new URLSearchParams();
    if ($('searchInput').value.trim()) params.set('q', $('searchInput').value.trim());
    if ($('courseFilter').value) params.set('course', $('courseFilter').value);
    try {
        lectures = await Api.get('/api/lectures' + (params.toString() ? '?' + params : '')) || [];
        render();
    } catch (err) {
        $('pageError').textContent = 'Could not load lectures: ' + err.message;
        $('pageError').classList.remove('hidden');
    }
}

function render() {
    const filtered = $('searchInput').value.trim() || $('courseFilter').value;
    $('lectureCount').textContent = lectures.length + (lectures.length === 1 ? ' lecture' : ' lectures') +
        (filtered ? ' found' : ' available');

    $('emptyState').classList.toggle('hidden', lectures.length > 0);
    $('emptyText').textContent = filtered ? 'No lectures match your search.'
        : (isInstructor ? 'No lectures yet. Click “Upload lecture” to add the first one.' : 'No lectures have been uploaded yet.');

    $('lectureGrid').innerHTML = lectures.map(l => `
        <article class="card">
            <div class="card-thumb ${l.mediaType}" data-view="${l.id}">${ICONS[l.mediaType] || ICONS.OTHER}</div>
            <div class="card-body">
                ${l.course ? `<span class="chip">${escapeHtml(l.course)}</span>` : ''}
                <h3 class="card-title" data-view="${l.id}">${escapeHtml(l.title)}</h3>
                <p class="card-desc">${escapeHtml(l.description || '')}</p>
                <div class="card-meta">
                    ${escapeHtml(l.uploadedByName || l.uploadedBy)} · ${formatDate(l.uploadedAt)} · ${formatSize(l.fileSize)}
                </div>
            </div>
            <div class="card-actions">
                <button class="btn small" data-view="${l.id}">▶ Open</button>
                ${isInstructor ? `<button class="btn danger small" data-delete="${l.id}">Delete</button>` : ''}
            </div>
        </article>`).join('');
}

$('lectureGrid').addEventListener('click', e => {
    const viewId = e.target.closest('[data-view]')?.dataset.view;
    const deleteId = e.target.closest('[data-delete]')?.dataset.delete;
    if (viewId) openViewer(Number(viewId));
    if (deleteId) deleteLecture(Number(deleteId));
});

$('searchInput').addEventListener('input', debounce(loadLectures, 300));
$('courseFilter').addEventListener('change', loadLectures);

// ---------- Viewer ----------
function openViewer(id) {
    const l = lectures.find(x => x.id === id);
    if (!l) return;
    const src = Api.withToken(l.contentUrl);
    let html;
    switch (l.mediaType) {
        case 'VIDEO': html = `<video controls autoplay src="${src}"></video>`; break;
        case 'AUDIO': html = `<audio controls autoplay src="${src}"></audio>`; break;
        case 'PDF':   html = `<iframe src="${src}" title="${escapeHtml(l.title)}"></iframe>`; break;
        case 'IMAGE': html = `<img src="${src}" alt="${escapeHtml(l.title)}">`; break;
        default:
            html = `<div class="empty"><div class="icon">${ICONS.DOCUMENT}</div>
                    <p>This file type can't be previewed in the browser. Use <strong>Download</strong> to open it.</p></div>`;
    }
    $('viewerTitle').textContent = l.title;
    $('viewerContent').innerHTML = html;
    $('viewerInfo').innerHTML =
        `${l.course ? `<strong>${escapeHtml(l.course)}</strong> · ` : ''}${escapeHtml(l.fileName)} · ${formatSize(l.fileSize)}` +
        (l.description ? `<p>${escapeHtml(l.description)}</p>` : '');
    $('viewerDownload').href = Api.withToken(l.downloadUrl);
    openModal('viewerModal');
}

// ---------- Delete ----------
async function deleteLecture(id) {
    const l = lectures.find(x => x.id === id);
    if (!l || !confirm(`Delete lecture "${l.title}"? This cannot be undone.`)) return;
    try {
        await Api.del('/api/lectures/' + id);
        await Promise.all([loadCourses(), loadLectures()]);
    } catch (err) {
        alert('Delete failed: ' + err.message);
    }
}

// ---------- Upload ----------
function setFile(file) {
    selectedFile = file || null;
    $('fileName').textContent = file ? `${file.name} (${formatSize(file.size)})` : '';
    if (file && !$('upTitle').value) {
        $('upTitle').value = file.name.replace(/\.[^.]+$/, '').replace(/[_-]+/g, ' ');
    }
}

$('openUploadBtn').addEventListener('click', () => {
    $('uploadForm').reset();
    setFile(null);
    $('uploadError').classList.add('hidden');
    $('progress').classList.add('hidden');
    $('progressBar').style.width = '0';
    openModal('uploadModal');
});

$('dropzone').addEventListener('click', () => $('upFile').click());
$('upFile').addEventListener('change', e => setFile(e.target.files[0]));
['dragenter', 'dragover'].forEach(ev => $('dropzone').addEventListener(ev, e => {
    e.preventDefault();
    $('dropzone').classList.add('drag');
}));
['dragleave', 'drop'].forEach(ev => $('dropzone').addEventListener(ev, e => {
    e.preventDefault();
    $('dropzone').classList.remove('drag');
}));
$('dropzone').addEventListener('drop', e => setFile(e.dataTransfer.files[0]));

$('uploadForm').addEventListener('submit', async e => {
    e.preventDefault();
    $('uploadError').classList.add('hidden');
    if (!selectedFile) {
        $('uploadError').textContent = 'Please choose a file to upload.';
        $('uploadError').classList.remove('hidden');
        return;
    }
    const form = new FormData();
    form.append('file', selectedFile);
    form.append('title', $('upTitle').value);
    form.append('course', $('upCourse').value);
    form.append('description', $('upDescription').value);

    $('uploadBtn').disabled = true;
    $('progress').classList.remove('hidden');
    try {
        await Api.upload('/api/lectures', form, pct => { $('progressBar').style.width = pct + '%'; });
        closeModal('uploadModal');
        await Promise.all([loadCourses(), loadLectures()]);
    } catch (err) {
        $('uploadError').textContent = err.message;
        $('uploadError').classList.remove('hidden');
    } finally {
        $('uploadBtn').disabled = false;
    }
});

// ---------- Modal close buttons / backdrop / Esc ----------
document.querySelectorAll('[data-close]').forEach(btn =>
    btn.addEventListener('click', () => closeModal(btn.dataset.close)));
document.querySelectorAll('.modal-backdrop').forEach(bd =>
    bd.addEventListener('click', e => { if (e.target === bd) closeModal(bd.id); }));
document.addEventListener('keydown', e => {
    if (e.key === 'Escape') ['uploadModal', 'viewerModal'].forEach(closeModal);
});

// ---------- Init ----------
loadCourses().catch(() => {});
loadLectures();
