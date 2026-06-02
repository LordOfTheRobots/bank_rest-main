(function () {
    const LOGOUT_ENDPOINT = '/api/v1/auth/logout';
    const REDIRECT_URL = '/auth/sign-in?logged-out=true';
    const TOKEN_KEY = 'jwt_token';

    async function handleLogout() {
        console.log('Logout initiated');
        try {
            const token = localStorage.getItem(TOKEN_KEY);
            const headers = { 'Content-Type': 'application/json' };
            if (token) headers['Authorization'] = `Bearer ${token}`;

            await fetch(LOGOUT_ENDPOINT, {
                method: 'POST',
                headers,
                credentials: 'include'
            });
        } catch (e) {
            console.warn('⚠Server logout failed, proceeding with client cleanup:', e.message);
        } finally {
            clearAuthSession();
            window.location.href = REDIRECT_URL;
        }
    }
    function clearAuthSession() {
        console.log('Clearing auth session');
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem('adminEmail');
        localStorage.removeItem('userRole');

        const cookieNames = ['refreshToken', 'jwt', 'auth_token', 'JSESSIONID'];
        cookieNames.forEach(name => {
            document.cookie = `${name}=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT; SameSite=Lax`;
            document.cookie = `${name}=; Path=/api/v1/auth/refresh; Expires=Thu, 01 Jan 1970 00:00:00 GMT; SameSite=Strict`;
        });
    }

    window.handleLogout = handleLogout;
    window.clearAuthSession = clearAuthSession;
})();