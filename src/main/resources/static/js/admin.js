(function() {
    'use strict';

    const API_BASE = '/api/v1/admin';
    const TOKEN_KEY = 'jwt_token';
    const state = {
        page: { cards: 0, users: 0, txns: 0 },
        size: 10,
        loaded: { cards: false, users: false, txns: false, types: false, templates: false },
        edit: { cardId: null, userId: null, typeCode: null, tplId: null },
        confirmCb: null
    };

    function showAlert(msg, type = 'success') {
        const el = document.getElementById(`alert-${type}`);
        if (!el) return;
        el.textContent = msg; el.classList.add('show');
        setTimeout(() => el.classList.remove('show'), 4000);
    }
    function openModal(id) { document.getElementById(id)?.classList.add('active'); }
    function closeModal(id) { document.getElementById(id)?.classList.remove('active'); }

    const truncate = (s, n) => s && s.length > n ? s.slice(0, n) + '...' : (s || '-');
    const mask = n => n && n.length > 8 ? n.slice(0,4) + ' **** **** ' + n.slice(-4) : (n || '-');
    const fmtDate = d => d ? new Date(d).toLocaleString('ru-RU') : '-';

    async function api(endpoint, opts = {}) {
        try {
            const res = await fetch(`${API_BASE}${endpoint}`, { ...opts, headers: opts.headers || {} });
            if (res.status === 401 || res.status === 403) {
                localStorage.removeItem(TOKEN_KEY);
                window.location.href = '/auth/sign-in?error=expired';
                throw new Error('Сессия истекла');
            }
            if (!res.ok) {
                const e = await res.json().catch(() => ({ message: `Ошибка ${res.status}` }));
                throw new Error(e.message || `HTTP ${res.status}`);
            }
            return res.headers.get('content-type')?.includes('application/json') ? await res.json() : null;
        } catch (err) {
            showAlert(err.message, 'error');
            throw err;
        }
    }

    async function loadCards(p = 0) {
        state.page.cards = p;
        const tb = document.getElementById('cards-tbody'); if (!tb) return;
        tb.innerHTML = '<tr><td colspan="7" class="loading">Загрузка...</td></tr>';
        try {
            const q = new URLSearchParams({ page: p, size: state.size });
            const s = document.getElementById('card-search')?.value, st = document.getElementById('card-status')?.value;
            if (s) q.append('cardNumber', s); if (st) q.append('status', st);
            const data = await api(`/cards?${q}`);
            renderCards(data?.content || [], tb);
            renderPagination(data, 'cards', loadCards);
        } catch {}
    }
    function renderCards(arr, tb) {
        if (!arr.length) return tb.innerHTML = '<tr><td colspan="7" class="loading">Нет данных</td></tr>';
        tb.innerHTML = arr.map(c => {
            const st = c.condition?.[0]?.conditionName || 'UNKNOWN';
            return `<tr data-id="${c.cardId}">
                <td>${truncate(c.cardId, 8)}</td><td>${mask(c.cardNumber)}</td>
                <td>${c.expireDate || '-'}</td><td>${c.balance?.toFixed(2) || '0.00'} ₽</td>
                <td><span class="status-badge status-${st.toLowerCase()}">${st}</span></td>
                <td>${c.user?.email || c.user?.userId || '-'}</td>
                <td class="actions">
                    ${st !== 'BLOCKED' ? `<button class="btn btn-sm btn-warning" data-act="block" data-id="${c.cardId}"></button>` : ''}
                    <button class="btn btn-sm btn-danger" data-act="del" data-type="card" data-id="${c.cardId}">🗑</button>
                </td>
            </tr>`;
        }).join('');
    }

    async function loadUsers(p = 0) {
        state.page.users = p;
        const tb = document.getElementById('users-tbody'); if (!tb) return;
        tb.innerHTML = '<tr><td colspan="6" class="loading">Загрузка...</td></tr>';
        try {
            const q = new URLSearchParams({ page: p, size: state.size });
            const s = document.getElementById('user-search')?.value; if (s) q.append('email', s);
            const data = await api(`/users?${q}`);
            renderUsers(data?.content || [], tb);
            renderPagination(data, 'users', loadUsers);
        } catch {}
    }
    function renderUsers(arr, tb) {
        if (!arr.length) return tb.innerHTML = '<tr><td colspan="6" class="loading">Нет данных</td></tr>';
        tb.innerHTML = arr.map(u => `<tr data-id="${u.userId}">
            <td>${truncate(u.userId, 8)}</td><td>${u.email}</td><td>${u.telephoneNumber || '-'}</td>
            <td>${u.telegramId || '-'}</td><td>${u.roleName || u.role?.name || 'USER'}</td>
            <td class="actions">
                <button class="btn btn-sm btn-warning" data-act="edit-user" data-id="${u.userId}">✏️</button>
                <button class="btn btn-sm btn-danger" data-act="del" data-type="user" data-id="${u.userId}">🗑</button>
            </td>
        </tr>`).join('');
    }

    async function loadTxns(p = 0, cardId = null) {
        state.page.txns = p;
        const tb = document.getElementById('txn-tbody'); if (!tb) return;
        tb.innerHTML = '<tr><td colspan="6" class="loading">Загрузка...</td></tr>';
        try {
            const q = new URLSearchParams({ page: p, size: state.size });
            if (cardId) q.append('cardId', cardId);
            const data = await api(`/transactions?${q}`);
            renderTxns(data?.content || [], tb);
            renderPagination(data, 'txns', pg => loadTxns(pg, document.getElementById('txn-filter')?.value?.trim() || null));
            state.loaded.txns = true;
        } catch {}
    }
    function renderTxns(arr, tb) {
        if (!arr.length) return tb.innerHTML = '<tr><td colspan="6" class="loading">Нет данных</td></tr>';
        tb.innerHTML = arr.map(t => `<tr>
            <td>${truncate(t.transactionId?.toString(), 8)}</td><td>${fmtDate(t.transactionDate)}</td>
            <td><b>${t.amount?.toFixed(2) || '0.00'} ₽</b></td><td>${t.mainCardNumber || '-'}</td>
            <td>${t.secondaryCard || '-'}</td><td title="${t.description || ''}">${truncate(t.description, 30) || '-'}</td>
        </tr>`).join('');
    }

    async function loadTypes() {
        const tb = document.getElementById('types-tbody'); if (!tb) return;
        tb.innerHTML = '<tr><td colspan="4" class="loading">Загрузка...</td></tr>';
        try { const data = await api('/types'); renderTypes(data || [], tb); updateTypeFilter(data); } catch {}
    }
    function renderTypes(arr, tb) {
        if (!arr.length) return tb.innerHTML = '<tr><td colspan="4" class="loading">Нет данных</td></tr>';
        tb.innerHTML = arr.map(t => `<tr>
            <td><b>${t.code}</b></td><td>${t.supportedChannels?.join(', ') || '-'}</td><td>${t.templateCount}</td>
            <td class="actions">
                <button class="btn btn-sm btn-warning" data-act="edit-type" data-code="${t.code}">✏️</button>
                <button class="btn btn-sm btn-danger" data-act="del-type" data-code="${t.code}">🗑</button>
            </td>
        </tr>`).join('');
    }
    function updateTypeFilter(types) {
        const sel = document.getElementById('tpl-filter'); if (!sel) return;
        sel.innerHTML = '<option value="">Все типы</option>';
        types.forEach(t => { const o = document.createElement('option'); o.value = t.code; o.textContent = t.code; sel.appendChild(o); });
    }

    async function loadTemplates(code = null) {
        const tb = document.getElementById('tpl-tbody'); if (!tb) return;
        tb.innerHTML = '<tr><td colspan="5" class="loading">Загрузка...</td></tr>';
        try { const q = code ? `?typeCode=${code}` : ''; const data = await api(`/templates${q}`); renderTemplates(data || [], tb); } catch {}
    }
    function renderTemplates(arr, tb) {
        if (!arr.length) return tb.innerHTML = '<tr><td colspan="5" class="loading">Нет данных</td></tr>';
        tb.innerHTML = arr.map(t => `<tr>
            <td>${t.id}</td><td><b>${t.typeCode}</b></td><td>${t.locale}</td>
            <td title="${t.title}">${truncate(t.title, 30)}</td>
            <td class="actions">
                <button class="btn btn-sm btn-warning" data-act="edit-tpl" data-id="${t.id}">✏️</button>
                <button class="btn btn-sm btn-danger" data-act="del-tpl" data-id="${t.id}">🗑</button>
            </td>
        </tr>`).join('');
    }

    function renderPagination(data, type, loadFn) {
        const cont = document.getElementById(`${type}-pagination`);
        if (!cont || !data?.totalPages || data.totalPages <= 1) { cont.innerHTML = ''; return; }
        const p = data.number;
        cont.innerHTML = `
            <button data-page="prev" ${p === 0 ? 'disabled' : ''}>←</button>
            <span>Стр. ${p + 1} / ${data.totalPages}</span>
            <button data-page="next" ${p >= data.totalPages - 1 ? 'disabled' : ''}>→</button>
        `;
        cont.querySelectorAll('button').forEach(btn =>
            btn.addEventListener('click', () => loadFn(btn.dataset.page === 'prev' ? p - 1 : p + 1))
        );
    }

    async function saveCard() {
        const btn = document.getElementById('btn-save-card'); const form = document.getElementById('form-card');
        if (!form?.checkValidity()) return form.reportValidity(); btn.disabled = true;
        try {
            const payload = { userId: document.getElementById('f-card-user').value, card: { cardNumber: document.getElementById('f-card-num').value, expirationDate: document.getElementById('f-card-exp').value } };
            await api(state.edit.cardId ? `/card/${state.edit.cardId}` : '/card', { method: state.edit.cardId ? 'PATCH' : 'POST', body: JSON.stringify(payload) });
            showAlert(state.edit.cardId ? 'Обновлено' : 'Создано'); closeModal('modal-card'); loadCards(0); form.reset(); state.edit.cardId = null;
        } finally { btn.disabled = false; }
    }
    async function saveUser() {
        const btn = document.getElementById('btn-save-user'); const form = document.getElementById('form-user');
        if (!form?.checkValidity()) return form.reportValidity(); btn.disabled = true;
        try {
            const p = { email: document.getElementById('f-user-email').value, telephoneNumber: document.getElementById('f-user-phone').value||null, telegramId: document.getElementById('f-user-tg').value||null, roleName: document.getElementById('f-user-role').value };
            const pass = document.getElementById('f-user-pass').value; if (pass) p.password = pass;
            await api('/user', { method: state.edit.userId ? 'PATCH' : 'POST', body: JSON.stringify(state.edit.userId ? { ...p, userId: state.edit.userId } : p) });
            showAlert(state.edit.userId ? 'Обновлено' : 'Создано'); closeModal('modal-user'); loadUsers(0); form.reset(); state.edit.userId = null;
        } finally { btn.disabled = false; }
    }
    async function saveType() {
        const btn = document.getElementById('btn-save-type'); const code = document.getElementById('f-type-code').value.trim();
        const ch = document.getElementById('f-type-channels').value.split(',').map(s=>s.trim()).filter(Boolean);
        if (!code || (!state.edit.typeCode && !ch.length)) return showAlert('Заполните код и каналы', 'error');
        btn.disabled = true;
        try {
            const method = state.edit.typeCode ? 'PATCH' : 'POST';
            const ep = state.edit.typeCode ? `/types/${state.edit.typeCode}` : '/types';
            const payload = state.edit.typeCode ? { supportedChannels: ch } : { code, supportedChannels: ch };
            await api(ep, { method, body: JSON.stringify(payload) });
            showAlert('Сохранено'); closeModal('modal-type'); loadTypes(); state.edit.typeCode = null;
        } finally { btn.disabled = false; }
    }
    async function saveTemplate() {
        const btn = document.getElementById('btn-save-tpl');
        const title = document.getElementById('f-tpl-title').value.trim();
        const body = document.getElementById('f-tpl-body').value.trim();
        const locale = document.getElementById('f-tpl-locale').value.trim();
        const typeCode = document.getElementById('f-tpl-type').value.trim();
        if (!typeCode || !title || !body) return showAlert('Заполните обязательные поля', 'error');
        btn.disabled = true;
        try {
            const method = state.edit.tplId ? 'PATCH' : 'POST';
            const ep = state.edit.tplId ? `/templates/${state.edit.tplId}` : '/templates';
            const payload = state.edit.tplId ? { title, body, locale } : { typeCode, locale, title, body };
            await api(ep, { method, body: JSON.stringify(payload) });
            showAlert('Сохранено'); closeModal('modal-tpl'); loadTemplates(document.getElementById('tpl-filter')?.value || null); state.edit.tplId = null;
        } finally { btn.disabled = false; }
    }
    function confirmDelete(type, id, cb) {
        document.getElementById('confirm-msg').textContent = `Удалить ${type}?`;
        state.confirmCb = cb; openModal('modal-confirm');
    }

    function setupTabs() {
        const tabBtns = document.querySelectorAll('.tab-btn[data-tab]');
        const sections = document.querySelectorAll('.section');
        tabBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                tabBtns.forEach(b => b.classList.remove('active'));
                sections.forEach(s => s.classList.remove('active'));
                btn.classList.add('active');
                const target = btn.dataset.tab;
                const sectionId = target === 'txns' ? 'transactions-section' : `${target}-section`;
                document.getElementById(sectionId)?.classList.add('active');
                if (!state.loaded[target]) {
                    if (target === 'cards') loadCards(0);
                    if (target === 'users') loadUsers(0);
                    if (target === 'txns') loadTxns(0);
                    if (target === 'types') loadTypes();
                    if (target === 'templates') loadTemplates();
                    state.loaded[target] = true;
                }
            });
        });
    }

    function handleTableAction(e, type, loadFn) {
        const btn = e.target.closest('[data-act]'); if (!btn) return;
        const id = btn.dataset.id || btn.dataset.code; const act = btn.dataset.act;
        if (act === 'block') api('/block-card', { method: 'POST', body: JSON.stringify(Number(id)) }).then(() => { showAlert('Заблокировано'); loadFn(state.page[type]); });
        else if (act === 'del') {
            const typeRu = type === 'cards' ? 'карту' : type === 'users' ? 'пользователя' : 'запись';
            confirmDelete(typeRu, id, () => api(type === 'cards' ? '/card' : type === 'users' ? '/user' : `/types/${id}`, { method: 'DELETE' }).then(() => { showAlert('Удалено'); loadFn(state.page[type] || 0); }));
        } else if (act === 'edit-user') {
            state.edit.userId = id;
            api(`/user/${id}`).then(u => {
                document.getElementById('user-modal-title').textContent = 'Редактировать пользователя';
                document.getElementById('f-user-email').value = u.email || '';
                document.getElementById('f-user-phone').value = u.telephoneNumber || '';
                document.getElementById('f-user-tg').value = u.telegramId || '';
                document.getElementById('f-user-role').value = u.roleName || u.role?.name || 'USER';
                document.getElementById('f-user-pass').value = ''; document.getElementById('f-user-pass').required = false;
                document.getElementById('pass-hint').textContent = '(оставьте пустым, чтобы не менять)';
                openModal('modal-user');
            });
        }
    }

    document.addEventListener('DOMContentLoaded', () => {
        setupTabs();
        document.getElementById('cards-tbody')?.addEventListener('click', e => handleTableAction(e, 'cards', loadCards));
        document.getElementById('users-tbody')?.addEventListener('click', e => handleTableAction(e, 'users', loadUsers));
        document.querySelectorAll('[data-modal]').forEach(el => el.addEventListener('click', () => closeModal(el.dataset.modal)));
        document.querySelectorAll('.modal-overlay').forEach(ov => ov.addEventListener('click', e => { if (e.target === ov) ov.classList.remove('active'); }));
        document.getElementById('btn-save-card')?.addEventListener('click', saveCard);
        document.getElementById('btn-save-user')?.addEventListener('click', saveUser);
        document.getElementById('btn-save-type')?.addEventListener('click', saveType);
        document.getElementById('btn-save-tpl')?.addEventListener('click', saveTemplate);
        document.getElementById('btn-add-card')?.addEventListener('click', () => { state.edit.cardId = null; openModal('modal-card'); });
        document.getElementById('btn-add-user')?.addEventListener('click', () => { state.edit.userId = null; openModal('modal-user'); });
        document.getElementById('btn-add-type')?.addEventListener('click', () => { state.edit.typeCode = null; openModal('modal-type'); });
        document.getElementById('btn-add-template')?.addEventListener('click', () => { state.edit.tplId = null; openModal('modal-tpl'); });
        document.getElementById('btn-confirm-yes')?.addEventListener('click', () => { if (state.confirmCb) state.confirmCb(); closeModal('modal-confirm'); state.confirmCb = null; });
        document.getElementById('btn-txn-apply')?.addEventListener('click', () => { state.loaded.txns = false; loadTxns(0, document.getElementById('txn-filter')?.value?.trim() || null); });
        document.getElementById('tpl-filter')?.addEventListener('change', e => loadTemplates(e.target.value || null));
        loadCards(0);
    });
})();