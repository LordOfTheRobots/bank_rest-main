const API_BASE = '/api/v1/user';
const TOKEN_KEY = 'jwt_token';

async function api(endpoint, opts = {}) {
  const token = localStorage.getItem(TOKEN_KEY);
  const headers = { 'Content-Type': 'application/json', ...(token && { Authorization: `Bearer ${token}` }), ...opts.headers };
  try {
    const res = await fetch(`${API_BASE}${endpoint}`, { ...opts, headers });
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
  } catch (err) { showAlert(err.message, 'error'); throw err; }
}

function showAlert(msg, type = 'success') {
  const el = document.getElementById(`alert-${type}`); if (!el) return;
  el.textContent = msg; el.classList.add('show'); setTimeout(() => el.classList.remove('show'), 4000);
}

document.addEventListener('DOMContentLoaded', async () => {
  if (!localStorage.getItem(TOKEN_KEY)) { window.location.href = '/auth/sign-in?error=unauthorized'; return; }

  const select = document.getElementById('src-card');
  const form = document.getElementById('txn-form');
  const btn = document.getElementById('btn-submit');
  const status = document.getElementById('status-msg');

  try {
    const data = await api('/cards?page=0&size=50');
    const cards = data?.content || [];
    select.innerHTML = '';
    if (!cards.length) {
      select.innerHTML = '<option value="" disabled>Нет доступных карт</option>';
      btn.disabled = true;
      status.textContent = 'Добавьте карту, чтобы совершить перевод';
      return;
    }
    cards.forEach(c => {
      const opt = document.createElement('option');
      opt.value = c.cardId;
      const num = c.cardNumber || '****';
      const masked = num.length > 8 ? num.slice(0,4) + ' **** **** ' + num.slice(-4) : num;
      opt.textContent = `${masked} (Баланс: ${c.balance?.toFixed(2) || '0.00'} ₽)`;
      select.appendChild(opt);
    });
  } catch (e) {
    select.innerHTML = '<option value="" disabled>Ошибка загрузки карт</option>';
    status.textContent = 'Не удалось загрузить карты';
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    status.textContent = ''; status.className = 'status-msg';
    btn.disabled = true; btn.textContent = 'Обработка...';

    const payload = {
      cardId: Number(select.value),
      cardToTransact: document.getElementById('dest-card').value.trim(),
      amount: parseFloat(document.getElementById('amount').value),
      description: document.getElementById('desc').value.trim() || null
    };

    if (!payload.cardId || !payload.cardToTransact || payload.amount <= 0) {
      status.textContent = 'Проверьте правильность введённых данных'; status.className = 'status-msg error';
      btn.disabled = false; btn.textContent = 'Отправить перевод'; return;
    }

    try {
      await fetch('/api/v1/transaction/make-transaction', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      });
      showAlert('Перевод успешно выполнен!');
      form.reset();
      status.textContent = 'Готово!'; status.className = 'status-msg success';
      setTimeout(() => location.reload(), 1500);
    } catch (e) {
      status.textContent = e.message; status.className = 'status-msg error';
    } finally {
      btn.disabled = false; btn.textContent = 'Отправить перевод';
    }
  });

  document.getElementById('btn-logout').onclick = typeof window.handleLogout === 'function'
    ? window.handleLogout : () => { localStorage.removeItem(TOKEN_KEY); location.href='/auth/sign-in'; };
});