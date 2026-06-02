document.addEventListener('DOMContentLoaded', async () => {
    const TOKEN_KEY = 'jwt_token';
    if (!localStorage.getItem(TOKEN_KEY)) {
        window.location.href = '/auth/sign-in?error=unauthorized';
        return;
    }

    const els = {
        avatar: document.getElementById('avatar-text'),
        name: document.getElementById('profile-name'),
        role: document.getElementById('profile-role'),
        email: document.getElementById('p-email'),
        phone: document.getElementById('p-phone'),
        tg: document.getElementById('p-tg'),
        locale: document.getElementById('p-locale'),
        uid: document.getElementById('p-user-id'),
        adminBtn: document.getElementById('btn-admin'),
        myCardsBtn: document.getElementById('btn-my-cards'),
        navCardsBtn: document.getElementById('btn-nav-cards'),
        statCards: document.getElementById('stat-cards'),
        statSpent: document.getElementById('stat-spent'),
        statNotifs: document.getElementById('stat-notifs'),
        modal: document.getElementById('modal-settings')
    };

    if (els.name) els.name.textContent = 'Загрузка...';

    try {
        const res = await fetch('/api/v1/user/me');
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const user = await res.json();

        const email = user.email || 'user@bankcards.com';
        const localPart = email.split('@')[0];

        if (els.avatar) els.avatar.textContent = localPart.slice(0, 2).toUpperCase();
        if (els.name) els.name.textContent = localPart.replace(/[._-]/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
        if (els.email) els.email.textContent = email;
        if (els.phone) els.phone.textContent = user.telephoneNumber || '—';
        if (els.tg) els.tg.textContent = user.telegramId || '—';
        if (els.locale) els.locale.textContent = (user.locale || 'ru').toUpperCase();
        if (els.uid && user.userId) els.uid.textContent = user.userId.length > 12 ? user.userId.slice(0, 8) + '…' : user.userId;

        const rawRole = user.roleName || user.role?.name || user.role || 'USER';
        const roleName = String(rawRole).toUpperCase().trim();

        if (els.role) {
            els.role.textContent = roleName;
            els.role.style.background = roleName === 'ADMIN' ? '#dbeafe' : '#f1f5f9';
            els.role.style.color = roleName === 'ADMIN' ? '#1e40af' : '#64748b';
        }

        if (els.adminBtn) {
            els.adminBtn.style.display = roleName === 'ADMIN' ? 'inline-flex' : 'none';
        }

        if (els.statCards) els.statCards.textContent = '—';
        if (els.statSpent) els.statSpent.textContent = '—';
        if (els.statNotifs) els.statNotifs.textContent = Array.isArray(user.notificationNames) ? user.notificationNames.length : '—';

    } catch (err) {
        console.error('Profile fetch failed:', err);
        if (els.name) els.name.textContent = 'Ошибка загрузки';
        showToast('Не удалось загрузить профиль', 'error');
    }

    if (els.adminBtn) {
        els.adminBtn.onclick = () => authRedirect('/admin');
    }

    if (els.myCardsBtn) {
        els.myCardsBtn.addEventListener('click', async (e) => {
            e.preventDefault();
            try {
                const res = await fetch('/cards', { headers: { 'Accept': 'text/html' } });
                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                const html = await res.text();
                document.open(); document.write(html); document.close();
            } catch (err) {
                console.error('Ошибка загрузки /cards:', err);
                showToast('Не удалось загрузить страницу карт', 'error');
            }
        });
    }

    const logoutBtn = document.getElementById('btn-logout');
    if (logoutBtn) {
        logoutBtn.onclick = typeof window.handleLogout === 'function'
            ? window.handleLogout
            : () => { localStorage.removeItem(TOKEN_KEY); window.location.href = '/auth/sign-in'; };
    }

    const settingsBtn = document.getElementById('btn-settings');
    if (settingsBtn && els.modal) {
        settingsBtn.onclick = () => els.modal.classList.remove('hidden');
        document.querySelectorAll('[data-close]').forEach(el => {
            el.onclick = () => els.modal.classList.add('hidden');
        });
    }
});

function showToast(msg, type = 'success') {
    const toast = document.getElementById('toast');
    if (!toast) return console.warn('[Toast] Элемент #toast не найден');
    toast.textContent = msg;
    toast.className = `toast ${type === 'error' ? 'error' : ''}`;
    toast.classList.remove('hidden');
    setTimeout(() => toast.classList.add('hidden'), 3500);
}

async function authRedirect(url) {
    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: { 'Accept': 'text/html,application/xhtml+xml' },
            credentials: 'include'
        });

        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const html = await response.text();

        history.pushState({}, '', url);
        document.open();
        document.write(html);
        document.close();
    } catch (e) {
        console.error('authRedirect failed:', e);
        window.location.href = url;
    }
}