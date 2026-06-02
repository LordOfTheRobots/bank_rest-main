(function() {
    'use strict';

    const API_BASE = '/api/v1/user';
    const TOKEN_KEY = 'jwt_token';

    const state = {
        currentCardId: null,
        txnPage: 0,
        uploadCardId: null,
        selectedFile: null
    };
    let autoRefreshInterval = null;

    function startAutoRefresh(intervalMs = 15000) {
        stopAutoRefresh();
        autoRefreshInterval = setInterval(() => {
            const hasActiveModal = document.querySelector('.modal.active');
            if (!hasActiveModal && document.visibilityState === 'visible') {
                refreshCards(true);
            }
        }, intervalMs);
        console.log('Auto-refresh started');
    }

    function stopAutoRefresh() {
        if (autoRefreshInterval) {
            clearInterval(autoRefreshInterval);
            autoRefreshInterval = null;
            console.log('Auto-refresh stopped');
        }
    }

    async function refreshCards(silent = false) {
        const grid = document.getElementById('cards-grid');
        if (!grid) return;

        if (!silent) {
            grid.innerHTML = '<div class="loading">Загрузка карт...</div>';
        }

        try {
            const data = await api('/cards?page=0&size=50');
            renderCards(data?.content || []);
            loadCardImages();

            if (state.currentCardId && !document.getElementById('txn-section')?.classList.contains('hidden')) {
                loadTransactions(state.currentCardId, state.txnPage);
            }
        } catch (e) {
            if (!silent) {
                grid.innerHTML = `<div class="loading error">Не удалось загрузить карты: ${e.message}</div>`;
            }
        }
    }
    async function api(endpoint, opts = {}) {
        try {
            const res = await fetch(`${API_BASE}${endpoint}`, opts);

            if (res.status === 401 || res.status === 403) {
                localStorage.removeItem(TOKEN_KEY);
                window.location.href = '/auth/sign-in';
                throw new Error('Unauthorized');
            }

            if (!res.ok) {
                const err = await res.json().catch(() => ({ message: `Ошибка ${res.status}` }));
                throw new Error(err.message || `Ошибка сервера`);
            }

            const ct = res.headers.get('content-type');
            return ct?.includes('application/json') ? await res.json() : null;
        } catch (err) {
            if (err.message !== 'Unauthorized') showToast(err.message, 'error');
            throw err;
        }
    }

    function showToast(msg, type = 'success') {
        const container = document.getElementById('toast-container') || (() => {
            const c = document.createElement('div');
            c.id = 'toast-container';
            c.className = 'toast-container';
            document.body.appendChild(c);
            return c;
        })();

        const id = `toast-${type}-${Date.now()}`;
        const toast = document.createElement('div');
        toast.id = id;
        toast.className = `toast toast--${type}`;
        toast.innerHTML = `<span class="toast__msg">${msg}</span><button class="toast__close">&times;</button>`;
        toast.querySelector('.toast__close').onclick = () => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        };
        container.appendChild(toast);
        requestAnimationFrame(() => toast.classList.add('show'));
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    }

    function openModal(id) {
        document.getElementById(id)?.classList.add('active');
        document.body.style.overflow = 'hidden';
    }
    function closeModal(id) {
        document.getElementById(id)?.classList.remove('active');
        document.body.style.overflow = '';
    }

    const fmtDate = d => d ? new Date(d).toLocaleString('ru-RU', { day:'2-digit', month:'2-digit', hour:'2-digit', minute:'2-digit' }) : '—';
    const maskNum = n => n?.length > 8 ? `${n.slice(0,4)} **** **** ${n.slice(-4)}` : (n || '');
    const fmtBalance = a => new Intl.NumberFormat('ru-RU', { style:'currency', currency:'RUB' }).format(a || 0);

    async function loadCards() {
        const grid = document.getElementById('cards-grid');
        if (!grid) return;
        grid.innerHTML = '<div class="loading">Загрузка карт...</div>';
        try {
            const data = await api('/cards?page=0&size=50');
            renderCards(data?.content || []);
            loadCardImages();
        } catch (e) {
            grid.innerHTML = `<div class="loading error">Не удалось загрузить карты: ${e.message}</div>`;
        }
    }

    async function loadCardImages() {
        const imgs = document.querySelectorAll('img.lazy-img');
        for (const img of imgs) {
            const cardId = img.dataset.cardId;
            if (!cardId) continue;
            try {
                const data = await api(`/cards/${cardId}/image`);
                if (data?.url && typeof data.url === 'string') {
                    img.src = data.url;
                    img.classList.remove('lazy-img');
                    img.style.opacity = '1';
                }
            } catch (e) {
                console.warn(`Image load failed for card ${cardId}:`, e);
                img.style.display = 'none';
            }
        }
    }

    function renderCards(cards) {
        const grid = document.getElementById('cards-grid');
        if (!grid) return;
        if (!cards.length) {
            grid.innerHTML = '<div class="loading">Карт нет. Добавьте первую!</div>';
            return;
        }

        grid.innerHTML = cards.map(c => {
            const status = c.cardCondition?.conditionName || 'UNKNOWN';
            const statusCls = status === 'BLOCKED' ? 'status-blocked'
                              : status === 'ACTIVE' ? 'status-active'
                              : 'status-unknown';

            return `
            <article class="card-item" data-card-id="${c.cardId}">
                <img class="card-bg lazy-img" data-card-id="${c.cardId}" alt="bg" loading="lazy" style="opacity:0;transition:opacity 0.3s">
                <div class="card-content">
                    <span class="card-status ${statusCls}">${status}</span>
                    <div class="card-number">${maskNum(c.cardNumber)}</div>
                    <div class="card-meta">
                        <div class="card-balance">${fmtBalance(c.balance)}</div>
                        <div class="card-exp">Exp: ${c.expireDate || c.expirationDate || '—'}</div>
                    </div>
                    <div class="card-actions">
                        <button class="btn btn-secondary btn-sm" data-act="txn">Транзакции</button>
                        <button class="btn btn-secondary btn-sm" data-act="upload">Фото</button>
                        <button class="btn btn-${status==='BLOCKED'?'primary':'warning'} btn-sm" data-act="block">
                            ${'Блок.'}
                        </button>
                    </div>
                </div>
            </article>`;
        }).join('');
    }

    async function loadTransactions(cardId, page = 0) {
        state.currentCardId = cardId;
        state.txnPage = page;
        const sec = document.getElementById('txn-section');
        if (!sec) return;
        sec.classList.remove('hidden');

        loadSpendingChart(cardId);

        document.getElementById('txn-list').innerHTML = '<div class="loading">Загрузка транзакций...</div>';
        document.getElementById('txn-title').textContent = `Транзакции карты #${String(cardId).slice(-4)}`;

        const oldBtn = document.querySelector('.btn-new-txn');
        if (oldBtn) oldBtn.remove();

        try {
            const data = await api(`/cards/${cardId}/transactions?page=${page}&size=10`);
            renderTransactions(data?.content || [], data);
        } catch (e) {
            sec.classList.add('hidden');
        }
    }

    async function loadSpendingChart(cardId) {
        const container = document.getElementById('spending-chart');
        if (!container) return;
        container.innerHTML = '<div class="chart-loading">Загрузка статистики...</div>';

        const today = new Date();
        const endDate = today.toISOString().split('T')[0];
        const startDate = new Date(today);
        startDate.setDate(today.getDate() - 6);
        const startStr = startDate.toISOString().split('T')[0];

        try {
            const data = await api(`/cards/${cardId}/spending?startDate=${startStr}&endDate=${endDate}`);
            renderSpendingChart(data?.days || [], container);
        } catch (e) {
            container.innerHTML = `<div class="chart-empty">Не удалось загрузить график: ${e.message}</div>`;
        }
    }

    function renderSpendingChart(days, container) {
        if (!days.length) {
            container.innerHTML = '<div class="chart-empty">Нет трат за последние 7 дней</div>';
            return;
        }

        const maxAmount = Math.max(...days.map(d => d.amount || 0), 1);
        const fmtRub = new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', maximumFractionDigits: 0 });
        const total = days.reduce((sum, d) => sum + (d.amount || 0), 0);

        let html = `
            <div class="chart-header">
                <h3 class="chart-title">Траты за 7 дней</h3>
                <span class="chart-total">Итого: ${fmtRub.format(total)}</span>
            </div>
            <div class="chart-bars">`;

        days.forEach(d => {
            const dateObj = new Date(d.date);
            const label = dateObj.toLocaleDateString('ru-RU', { day: '2-digit', month: 'short' });
            const heightPercent = ((d.amount || 0) / maxAmount) * 100;
            const val = d.amount > 0 ? fmtRub.format(d.amount) : '';

            html += `
                <div class="chart-bar-wrapper">
                    <div class="chart-bar" style="height: ${Math.max(heightPercent, 2)}%">
                        ${val ? `<span class="chart-bar-value">${val}</span>` : ''}
                    </div>
                    <span class="chart-label">${label}</span>
                </div>`;
        });

        html += '</div>';
        container.innerHTML = html;
    }

    function renderTransactions(txns, pageData) {
        const list = document.getElementById('txn-list');
        const header = document.getElementById('txn-title');

        if (header && !header.querySelector('.btn-new-txn')) {
            const btn = document.createElement('button');
            btn.className = 'btn btn-primary btn-sm btn-new-txn';
            btn.textContent = 'Новая транзакция';
            btn.onclick = () => openNewTransactionModal(state.currentCardId);
            header.appendChild(btn);
        }

        list.innerHTML = txns.length
            ? txns.map(t => `<div class="txn-item">
                  <span class="txn-date">${fmtDate(t.transactionDate)}</span>
                  <span class="txn-desc">${t.description || 'Перевод'}</span>
                  <span class="txn-amount ${t.amount < 0 ? 'negative' : ''}">${t.amount?.toFixed(2) || '0.00'} ₽</span>
              </div>`).join('')
            : '<div class="loading">Транзакций нет</div>';

        const pag = document.getElementById('txn-pagination');
        if (!pageData?.totalPages || pageData.totalPages <= 1) { pag.innerHTML = ''; return; }
        const p = pageData.number;
        pag.innerHTML = `
            <button data-p="prev" ${p===0?'disabled':''}>←</button>
            <span>Стр. ${p+1} из ${pageData.totalPages}</span>
            <button data-p="next" ${p>=pageData.totalPages-1?'disabled':''}>→</button>`;
        pag.querySelectorAll('button').forEach(btn =>
            btn.addEventListener('click', () => loadTransactions(state.currentCardId, btn.dataset.p==='prev'?p-1:p+1))
        );
    }

    function openNewTransactionModal(cardId) {
        state.currentCardId = cardId;
        document.getElementById('f-txn-amount').value = '';
        document.getElementById('f-txn-desc').value = '';
        document.getElementById('f-txn-card-to').value = '';
        document.getElementById('txn-status').textContent = '';
        document.getElementById('txn-status').className = 'form__status';
        openModal('modal-new-txn');
        setTimeout(() => document.getElementById('f-txn-amount')?.focus(), 100);
    }

    async function doMakeTransaction() {
        const btn = document.getElementById('btn-do-txn');
        const amountStr = document.getElementById('f-txn-amount').value.trim().replace(',', '.');
        const description = document.getElementById('f-txn-desc').value.trim();
        const cardTo = document.getElementById('f-txn-card-to').value.trim().replace(/\s/g, '');
        const statusEl = document.getElementById('txn-status');

        const amount = parseFloat(amountStr);
        if (isNaN(amount) || amount <= 0) return showToast('Сумма должна быть > 0', 'error');
        if (!description) return showToast('Введите описание', 'error');
        if (cardTo && !/^\d{13,19}$/.test(cardTo)) return showToast('Неверный номер карты', 'error');

        btn.disabled = true;
        statusEl.textContent = 'Обработка...';
        statusEl.className = 'form__status';

        try {
                await fetch('/api/v1/transaction/make-transaction', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        cardId: state.currentCardId,
                        amount: amount,
                        description: description,
                        cardToTransact: cardTo || null
                    })
                });
                showToast('Транзакция выполнена');
                closeModal('modal-new-txn');
                refreshCards();
                loadTransactions(state.currentCardId, state.txnPage);
            } catch (e) {
            } finally {
                btn.disabled = false;
            }
    }

    function setupImagePreview() {
        const fileInput = document.getElementById('f-upload-file');
        const preview = document.getElementById('upload-preview');
        const previewImg = document.getElementById('preview-img');
        const area = document.getElementById('upload-area');
        const removeBtn = document.getElementById('btn-remove-preview');

        area?.addEventListener('click', () => fileInput?.click());
        fileInput?.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (!file) return;
            if (!file.type.match('image/jpeg|image/png')) return showToast('Только JPG/PNG', 'error');
            if (file.size > 5_242_880) return showToast('Файл > 5 МБ', 'error');
            state.selectedFile = file;
            const reader = new FileReader();
            reader.onload = (ev) => {
                previewImg.src = ev.target.result;
                preview.classList.remove('hidden');
                area.classList.add('hidden');
            };
            reader.readAsDataURL(file);
        });
        removeBtn?.addEventListener('click', () => {
            state.selectedFile = null;
            preview.classList.add('hidden');
            area.classList.remove('hidden');
            fileInput.value = '';
            document.getElementById('upload-status').textContent = '';
        });
    }

    async function doUpload() {
        const btn = document.getElementById('btn-do-upload');
        const file = state.selectedFile;
        const status = document.getElementById('upload-status');
        if (!file) return showToast('Выберите файл', 'error');

        btn.disabled = true;
        status.textContent = 'Загрузка...';
        status.className = 'form__status';

        try {
                const fd = new FormData();
                fd.append('file', file);
                await api(`/cards/${state.uploadCardId}/image`, { method: 'POST', body: fd });
                showToast('Фото загружено');
                closeModal('modal-upload');
                refreshCards();
            } catch (e) {
            } finally {
                btn.disabled = false;
            }
    }

    async function saveCard() {
        const btn = document.getElementById('btn-save-card');
        const num = document.getElementById('f-card-num').value.trim().replace(/\s/g, '');
        const exp = document.getElementById('f-card-exp').value.trim();

        if (!/^\d{13,19}$/.test(num)) return showToast('Неверный номер карты', 'error');
        if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(exp)) return showToast('Формат: ММ/ГГ', 'error');

        btn.disabled = true;
        try {
                await api('/cards', { method: 'POST', body: JSON.stringify({ cardNumber: num, expirationDate: exp }) });
                showToast('Карта добавлена');
                closeModal('modal-add');
                refreshCards();
                document.getElementById('f-card-num').value = '';
                document.getElementById('f-card-exp').value = '';
            } finally { btn.disabled = false; }
    }

    async function toggleBlockCard(cardId, currentlyActive) {
        if (!confirm(`Вы уверены, что хотите ${currentlyActive ? 'заблокировать' : 'разблокировать'} карту?`)) return;
        try {
            await api(`/cards/${cardId}/block`, { method: 'PATCH' });
            showToast(`Карта ${currentlyActive ? 'заблокирована' : 'разблокирована'}`);
            refreshCards();
        } catch {}
    }

    document.addEventListener('DOMContentLoaded', () => {
        if (!localStorage.getItem(TOKEN_KEY)) {
            window.location.href = '/auth/sign-in?error=unauthorized';
            return;
        }

        setupImagePreview();

        startAutoRefresh(15000);

        document.addEventListener('visibilitychange', () => {
            if (document.visibilityState === 'hidden') {
                stopAutoRefresh();
            } else if (localStorage.getItem(TOKEN_KEY)) {
                startAutoRefresh(15000);
                refreshCards(true);
            }
        });
        setupImagePreview();

        document.getElementById('btn-add-card')?.addEventListener('click', () => openModal('modal-add'));
        document.getElementById('btn-save-card').onclick = saveCard;
        document.getElementById('btn-do-upload').onclick = doUpload;
        document.getElementById('btn-do-txn').onclick = doMakeTransaction;
        document.getElementById('btn-profile')?.addEventListener('click', async (e) => {
                    e.preventDefault();
                    try {
                        const res = await fetch('/profile', { headers: { 'Accept': 'text/html' } });
                        if (!res.ok) throw new Error(`HTTP ${res.status}`);
                        const html = await res.text();
                        document.open(); document.write(html); document.close();
                    } catch (err) {
                        console.error('Ошибка загрузки /profile:', err);
                        showToast('Не удалось загрузить страницу профиля', 'error');
                    }
                });
        document.querySelectorAll('[data-modal]').forEach(b =>
            b.onclick = e => closeModal(e.currentTarget.dataset.modal)
        );
        document.querySelectorAll('.modal__overlay').forEach(ov =>
            ov.onclick = e => { if (e.target === ov) ov.closest('.modal')?.classList.remove('active'); }
        );

        document.getElementById('btn-close-txn')?.addEventListener('click', () =>
            document.getElementById('txn-section')?.classList.add('hidden')
        );
        document.getElementById('btn-logout')?.addEventListener('click', () => {
            localStorage.removeItem(TOKEN_KEY);
            location.href='/auth/sign-in';
        });

        document.getElementById('f-card-num')?.addEventListener('input', e => {
            e.target.value = e.target.value.replace(/\D/g, '').slice(0,16).replace(/(.{4})/g, '$1 ').trim();
        });
        document.getElementById('f-card-exp')?.addEventListener('input', e => {
            let v = e.target.value.replace(/\D/g, '').slice(0,4);
            e.target.value = v.length >= 3 ? v.slice(0,2)+'/'+v.slice(2) : v;
        });
        document.getElementById('f-txn-amount')?.addEventListener('input', e => {
            e.target.value = e.target.value.replace(/[^0-9.,]/g, '').replace(',', '.');
        });
        document.getElementById('f-txn-card-to')?.addEventListener('input', e => {
            e.target.value = e.target.value.replace(/\D/g, '').slice(0,16).replace(/(.{4})/g, '$1 ').trim();
        });

        document.getElementById('cards-grid').addEventListener('click', e => {
            const btn = e.target.closest('[data-act]');
            const card = e.target.closest('.card-item');
            if (!btn || !card) return;

            const id = card.dataset.cardId;
            const act = btn.dataset.act;

            if (act === 'txn') loadTransactions(id);
            if (act === 'upload') {
                state.uploadCardId = id;
                state.selectedFile = null;
                document.getElementById('upload-preview').classList.add('hidden');
                document.getElementById('upload-area').classList.remove('hidden');
                document.getElementById('f-upload-file').value = '';
                document.getElementById('upload-status').textContent = '';
                openModal('modal-upload');
            }
            if (act === 'block') {
                const isBlocked = card.querySelector('.card-status')?.classList.contains('status-blocked');
                toggleBlockCard(id, !isBlocked);
            }
        });

        document.addEventListener('keydown', e => {
            if (e.key === 'Escape') {
                document.querySelectorAll('.modal.active').forEach(m => closeModal(m.id));
            }
        });

        loadCards();
    });
})();