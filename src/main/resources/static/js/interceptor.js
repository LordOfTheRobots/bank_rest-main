// src/main/resources/static/js/interceptor.js
(function() {
    console.log('[Interceptor] 🚀 Инициализация...');
    const originalFetch = window.fetch;
    const REFRESH_URL = '/api/v1/auth/refresh';
    let refreshPromise = null;

    function notifyUser(msg, type = 'error') {
        const alertEl = document.getElementById(`alert-${type}`);
        if (alertEl) {
            alertEl.textContent = msg; alertEl.classList.add('show');
            setTimeout(() => alertEl.classList.remove('show'), 6000); return;
        }
        let toast = document.getElementById('session-toast');
        if (!toast) {
            toast = document.createElement('div'); toast.id = 'session-toast';
            toast.style.cssText = `position:fixed;top:20px;left:50%;transform:translateX(-50%);background:${type==='error'?'#fee2e2':'#dcfce7'};color:${type==='error'?'#991b1b':'#166534'};padding:1rem 1.5rem;border-radius:10px;border:1px solid #fecaca;z-index:9999;box-shadow:0 6px 16px rgba(0,0,0,0.12);font-family:system-ui,sans-serif;max-width:92%;text-align:center;transition:opacity 0.3s;`;
            document.body.appendChild(toast);
        }
        toast.textContent = msg; toast.style.opacity = '1';
        setTimeout(() => { toast.style.opacity = '0'; }, 7000);
    }

    window.fetch = async function(input, init = {}) {
        const url = typeof input === 'string' ? input : (input.url || '');
        console.log(`[Interceptor] 🌐 Запрос: ${init?.method || 'GET'} ${url}`);

        if (url === REFRESH_URL) {
            console.log('[Interceptor] ⏭️ Пропускаем запрос рефреша');
            return originalFetch(input, init);
        }

        const headers = new Headers(init.headers || {});
        const token = localStorage.getItem('jwt_token');
        console.log(`[Interceptor] 🔑 Токен найден: ${!!token}`);

        if (token && !headers.has('Authorization')) {
            headers.set('Authorization', `Bearer ${token}`);
            console.log('[Interceptor] ✅ Заголовок Authorization добавлен');
        }
        if (!(init.body instanceof FormData) && !headers.has('Content-Type')) {
            headers.set('Content-Type', 'application/json');
        }
        init.headers = headers;

        async function tryRefresh() {
            if (!refreshPromise) {
                refreshPromise = originalFetch(REFRESH_URL, { method: 'POST', headers: { 'Content-Type': 'application/json' } })
                    .then(async res => {
                        if (!res.ok) throw new Error(`Refresh failed: ${res.status}`);
                        const data = await res.json();
                        const newToken = data.token || data.accessToken || data.jwt;
                        localStorage.setItem('jwt_token', newToken);
                        console.log('[Interceptor] 🔄 Токен успешно обновлён');
                        return newToken;
                    })
                    .catch(err => {
                        console.warn('[Interceptor] ❌ Рефреш упал:', err);
                        localStorage.removeItem('jwt_token');
                        notifyUser('⏱️ Сессия истекла. Обновите страницу или войдите заново.');
                        return null;
                    })
                    .finally(() => { refreshPromise = null; });
            }
            return refreshPromise;
        }

        try {
            let response = await originalFetch(input, init);

            if (response.status === 401 || response.status === 403) {
                if (init.__alreadyRefreshed) {
                    notifyUser('❌ Ошибка авторизации. Повторите попытку позже.');
                    throw new Error('Auth failed after refresh');
                }
                console.log('[Interceptor] 🔓 Получен 401/403, запускаем рефреш...');
                const newToken = await tryRefresh();
                if (!newToken) throw new Error('Refresh failed');

                headers.set('Authorization', `Bearer ${newToken}`);
                init.__alreadyRefreshed = true;
                console.log('[Interceptor] 🔁 Повторяем исходный запрос с новым токеном');
                return originalFetch(input, init);
            }
            return response;
        } catch (error) {
            console.error('[Interceptor] 💥 Ошибка запроса:', error);
            throw error;
        }
    };
    console.log('[Interceptor] ✅ window.fetch успешно переопределён');
})();