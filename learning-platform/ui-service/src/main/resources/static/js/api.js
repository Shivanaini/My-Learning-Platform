/**
 * Small helper shared by all pages: stores the JWT and calls the backend
 * through the API gateway (same origin, so no CORS setup is needed).
 */
const Api = (() => {
    const TOKEN_KEY = 'lp_token';
    const USER_KEY = 'lp_user';

    function save(auth) {
        localStorage.setItem(TOKEN_KEY, auth.token);
        localStorage.setItem(USER_KEY, JSON.stringify(auth.user));
    }

    function token() {
        return localStorage.getItem(TOKEN_KEY);
    }

    function user() {
        try {
            return JSON.parse(localStorage.getItem(USER_KEY));
        } catch (e) {
            return null;
        }
    }

    function logout() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        window.location.href = '/index.html';
    }

    async function request(method, url, body) {
        const headers = {};
        if (token()) headers['Authorization'] = 'Bearer ' + token();
        if (body !== undefined && !(body instanceof FormData)) headers['Content-Type'] = 'application/json';

        const res = await fetch(url, {
            method,
            headers,
            body: body === undefined ? undefined : (body instanceof FormData ? body : JSON.stringify(body))
        });

        if (res.status === 401 && token() && !url.startsWith('/api/auth/login')) {
            logout(); // token expired
            return;
        }
        if (res.status === 204) return null;

        const data = await res.json().catch(() => null);
        if (!res.ok) {
            throw new Error((data && data.message) || ('Request failed (' + res.status + ')'));
        }
        return data;
    }

    /** Upload with progress callback (fetch has no upload progress, so XHR is used). */
    function upload(url, formData, onProgress) {
        return new Promise((resolve, reject) => {
            const xhr = new XMLHttpRequest();
            xhr.open('POST', url);
            xhr.setRequestHeader('Authorization', 'Bearer ' + token());
            xhr.upload.onprogress = e => {
                if (e.lengthComputable && onProgress) onProgress(Math.round(e.loaded * 100 / e.total));
            };
            xhr.onload = () => {
                let data = null;
                try { data = JSON.parse(xhr.responseText); } catch (e) { /* ignore */ }
                if (xhr.status >= 200 && xhr.status < 300) resolve(data);
                else if (xhr.status === 401) logout();
                else reject(new Error((data && data.message) || ('Upload failed (' + xhr.status + ')')));
            };
            xhr.onerror = () => reject(new Error('Network error during upload'));
            xhr.send(formData);
        });
    }

    /** Adds the token as a query param so <video>/<iframe> can load protected files. */
    function withToken(url) {
        return url + (url.includes('?') ? '&' : '?') + 'token=' + encodeURIComponent(token());
    }

    return {
        save, token, user, logout, withToken, upload,
        get: url => request('GET', url),
        post: (url, body) => request('POST', url, body),
        del: url => request('DELETE', url)
    };
})();
