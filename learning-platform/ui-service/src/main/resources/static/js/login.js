// Already logged in? Go straight to the dashboard.
if (Api.token()) {
    window.location.href = '/dashboard.html';
}

const $ = id => document.getElementById(id);

function showError(el, message) {
    el.textContent = message;
    el.classList.remove('hidden');
}

$('showRegister').addEventListener('click', e => {
    e.preventDefault();
    $('loginCard').classList.add('hidden');
    $('registerCard').classList.remove('hidden');
});

$('showLogin').addEventListener('click', e => {
    e.preventDefault();
    $('registerCard').classList.add('hidden');
    $('loginCard').classList.remove('hidden');
});

$('loginForm').addEventListener('submit', async e => {
    e.preventDefault();
    $('loginError').classList.add('hidden');
    $('loginBtn').disabled = true;
    try {
        const auth = await Api.post('/api/auth/login', {
            username: $('loginUsername').value,
            password: $('loginPassword').value
        });
        Api.save(auth);
        window.location.href = '/dashboard.html';
    } catch (err) {
        showError($('loginError'), err.message);
    } finally {
        $('loginBtn').disabled = false;
    }
});

$('registerForm').addEventListener('submit', async e => {
    e.preventDefault();
    $('registerError').classList.add('hidden');
    $('registerBtn').disabled = true;
    try {
        const auth = await Api.post('/api/auth/register', {
            fullName: $('regFullName').value,
            username: $('regUsername').value,
            email: $('regEmail').value || null,
            password: $('regPassword').value,
            role: $('regRole').value
        });
        Api.save(auth);
        window.location.href = '/dashboard.html';
    } catch (err) {
        showError($('registerError'), err.message);
    } finally {
        $('registerBtn').disabled = false;
    }
});
