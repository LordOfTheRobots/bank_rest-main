console.log('auth.js загружен');

const JWT_STORAGE_KEY = 'jwt_token';

function saveToken(rawToken) {
    const clean = rawToken?.replace(/^Bearer\s+/i, '').trim();
    if (clean) {
        localStorage.setItem(JWT_STORAGE_KEY, clean);
        console.log('Token saved (clean):', clean.substring(0, 20) + '...');
    }
}

function getToken() {
    return localStorage.getItem(JWT_STORAGE_KEY);
}

function getAuthHeader() {
    const token = getToken();
    return token ? `Bearer ${token}` : null;
}

async function authFetch(url, options = {}) {
    const authHeader = getAuthHeader();

    options.headers = {
        ...(options.headers || {}),
        'Content-Type': 'application/json'
    };

    if (authHeader) {
        options.headers['Authorization'] = authHeader;
        console.log('authFetch:', url, '→ Authorization: Bearer ***');
    } else {
        console.warn('authFetch: no token for', url);
    }

    if (!options.credentials) {
        options.credentials = 'include';
    }

    return fetch(url, options);
}

async function authRedirect(url) {
    const authHeader = getAuthHeader();

    if (!authHeader) {
        console.warn('authRedirect: no token, falling back to normal redirect');
        window.location.href = url;
        return;
    }

    console.log('authRedirect:', url, '→ Authorization: Bearer ***');

    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Accept': 'text/html,application/xhtml+xml'
            },
            credentials: 'include'
        });

        console.log('authRedirect response:', response.status);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const html = await response.text();

        history.pushState({}, '', url);

        document.open();
        document.write(html);
        document.close();

        console.log('authRedirect: page replaced');

    } catch (e) {
        console.error('authRedirect failed:', e);
        window.location.href = url;
    }
}

document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM готов, token in storage:', getToken() ? 'present' : 'NULL');

    const isSignUp = document.getElementById('email') && document.getElementById('confirm');
    const isSignIn = document.getElementById('email') && !document.getElementById('confirm');

    if (isSignUp) initSignUp();
    else if (isSignIn) initSignIn();

    if (getToken()) {
        console.log('Already authorized, redirecting to /');
        authRedirect('/profile');
    }
});

function initSignUp() {
    console.log('initSignUp');
    const btn = document.getElementById('btn');
    if (!btn) return;

    btn.addEventListener('click', (e) => { e.preventDefault(); doSignUp(); });
    document.querySelectorAll('input').forEach(inp => {
        inp.addEventListener('keypress', (e) => { if (e.key === 'Enter') doSignUp(); });
    });
}

function initSignIn() {
    console.log('initSignIn');
    const btn = document.getElementById('btn');
    if (!btn) return;

    btn.addEventListener('click', (e) => { e.preventDefault(); doSignIn(); });
    document.querySelectorAll('input').forEach(inp => {
        inp.addEventListener('keypress', (e) => { if (e.key === 'Enter') doSignIn(); });
    });
}

async function doSignUp() {
    console.log('doSignUp started');

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const confirm = document.getElementById('confirm').value;
    const errorEl = document.getElementById('error');
    const btn = document.getElementById('btn');

    errorEl.classList.remove('show');
    errorEl.textContent = '';

    if (!email || !password) { showError('Заполните все поля', errorEl); return; }
    if (password !== confirm) { showError('Пароли не совпадают', errorEl); return; }

    btn.disabled = true;
    btn.textContent = 'Отправка...';

    try {
        const response = await authFetch('/api/v1/auth/sign-up', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });

        console.log('Response status:', response.status);
        const data = await response.json();
        console.log('Response data:', data);

        if (!response.ok) throw new Error(data.message || 'Ошибка регистрации');

        const rawToken = data.jwt || response.headers.get('Authorization');
        if (rawToken) {
            saveToken(rawToken);

            await authRedirect('/');
        } else {
            throw new Error('JWT не получен от сервера');
        }

    } catch (e) {
        console.error('doSignUp error:', e);
        showError(e.message, errorEl);
    } finally {
        btn.disabled = false;
        btn.textContent = 'Зарегистрироваться';
    }
}

async function doSignIn() {
    console.log('doSignIn started');

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const errorEl = document.getElementById('error');
    const btn = document.getElementById('btn');

    errorEl.classList.remove('show');
    errorEl.textContent = '';

    if (!email || !password) { showError('Заполните все поля', errorEl); return; }

    btn.disabled = true;
    btn.textContent = 'Входим...';

    try {
        const response = await authFetch('/api/v1/auth/sign-in', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });

        console.log('Response status:', response.status);
        const data = await response.json();
        console.log('Response data:', data);

        if (!response.ok) throw new Error(data.message || 'Ошибка входа');

        const rawToken = data.jwt || response.headers.get('Authorization');
        if (rawToken) {
            saveToken(rawToken);
            await authRedirect('/');
        } else {
            throw new Error('JWT не получен');
        }

    } catch (e) {
        console.error('doSignIn error:', e);
        showError(e.message, errorEl);
    } finally {
        btn.disabled = false;
        btn.textContent = 'Войти';
    }
}

function extractToken(data, response) {
    if (data?.jwt) return data.jwt;
    const authHeader = response.headers.get('Authorization');
    if (authHeader) return authHeader.replace(/^Bearer\s+/i, '').trim();
    return null;
}

function showError(message, element) {
    if (element) {
        element.textContent = message;
        element.classList.add('show');
    }
    console.error(message);
}

window.authFetch = authFetch;
window.authRedirect = authRedirect;
window.getJwtToken = getToken;