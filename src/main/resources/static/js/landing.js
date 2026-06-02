document.addEventListener('DOMContentLoaded', () => {
      const token = localStorage.getItem('jwt_token');
      const nav = document.getElementById('auth-nav');
      const heroActions = document.querySelector('.hero-actions');

      if (!token || !nav) return;

      nav.innerHTML = '<a href="/profile" id="nav-profile" class="btn btn-primary">Профиль</a>';
      if (heroActions) heroActions.style.display = 'none';

      document.getElementById('nav-profile').addEventListener('click', async (e) => {
        e.preventDefault();
        try {
          const res = await fetch('/profile', {
            method: 'GET',
            headers: {
              'Authorization': `Bearer ${token}`,
              'Accept': 'text/html,application/xhtml+xml'
            }
          });

          if (!res.ok) throw new Error(`HTTP ${res.status}`);

          const html = await res.text();

          history.pushState({}, '', '/profile');
          document.open();
          document.write(html);
          document.close();
        } catch (err) {
          console.error('Profile redirect failed:', err);
          localStorage.removeItem('jwt_token');
          window.location.href = '/auth/sign-in?error=expired';
        }
      });
    });