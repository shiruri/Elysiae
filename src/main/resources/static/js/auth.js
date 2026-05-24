(function() {
  'use strict';

  function rel(path) {
    return /\/?(?:login|changepassword|index|reset-password)\.html$/.test(location.pathname) ? path : '../' + path;
  }

  window.showToast = window.showToast || function(message, type) {
    type = type || 'info';
    var ctn = document.getElementById('toast-ctn');
    if (!ctn) {
      ctn = document.createElement('div');
      ctn.id = 'toast-ctn';
      ctn.className = 'toast-ctn';
      document.body.appendChild(ctn);
    }
    var t = document.createElement('div');
    t.className = 'toast toast-' + type;
    var icons = {
      success: '<svg class="toast-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>',
      error: '<svg class="toast-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>',
      info: '<svg class="toast-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>'
    };
    t.innerHTML = (icons[type] || icons.info);
    var msgSpan = document.createElement('span');
    msgSpan.textContent = message;
    t.appendChild(msgSpan);
    ctn.appendChild(t);
    setTimeout(function() {
      t.classList.add('hiding');
      setTimeout(function() { if (t.parentNode) t.parentNode.removeChild(t); }, 250);
    }, 3500);
  };

  var publicPages = ['login', 'index', 'changepassword', 'reset-password', ''];

  function getPage() {
    return location.pathname.split('/').filter(Boolean).pop().replace('.html','') || '';
  }

  function isPublic() {
    return publicPages.indexOf(getPage()) !== -1;
  }

  window.Auth = {
    getUser: function() {
      try { return JSON.parse(localStorage.getItem('elysiae-user')); } catch(e) { return null; }
    },
    getToken: function() {
      return localStorage.getItem('elysiae-token');
    },
    isLoggedIn: function() {
      return !!this.getToken();
    },
    getRole: function() {
      var u = this.getUser();
      return u ? u.role : null;
    },
    getUserId: function() {
      var u = this.getUser();
      return u ? u.id : null;
    },
    login: function(username, password) {
      var self = this;
      return API.post('/api/auth/login', { username: username, password: password }).then(function(data) {
        localStorage.setItem('elysiae-token', data.token);
        localStorage.setItem('elysiae-user', JSON.stringify(data.user));
        return data;
      });
    },
    logout: function() {
      var self = this;
      var id = self.getUserId();
      if (id) {
        API.patch('/api/auth/logout/' + id).catch(function(){});
      }
      localStorage.removeItem('elysiae-token');
      localStorage.removeItem('elysiae-user');
      window.location.href = rel('login.html');
    },
    changePassword: function(oldPassword, newPassword) {
      var self = this;
      var id = this.getUserId();
      return API.patch('/api/auth/change-password/' + id, { oldPassword: oldPassword, newPassword: newPassword });
    },
    getMe: function() {
      var self = this;
      return API.get('/api/auth/me').then(function(data) {
        localStorage.setItem('elysiae-user', JSON.stringify(data));
        return data;
      });
    }
  };

  function guard() {
    if (isPublic()) {
      if (getPage() === 'changepassword') {
        if (!Auth.isLoggedIn()) {
          window.location.href = rel('login.html');
          return;
        }
        var user = Auth.getUser();
        if (user && !user.mustChangePassword) {
          window.location.href = rel('dashboard/dashboard.html');
          return;
        }
        return;
      }
      if (getPage() === 'login') {
        localStorage.removeItem('elysiae-token');
        localStorage.removeItem('elysiae-user');
      }
      return;
    }
    if (!Auth.isLoggedIn()) {
      window.location.href = rel('login.html');
      return;
    }
    var user = Auth.getUser();
    if (user && user.mustChangePassword) {
      window.location.href = rel('changepassword.html');
      return;
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', guard);
  } else {
    guard();
  }

  document.addEventListener('DOMContentLoaded', function() {
    var form = document.getElementById('login-form');
    if (!form) return;

    var btn = document.getElementById('login-btn');

    form.addEventListener('submit', function(e) {
      e.preventDefault();
      btn.disabled = true;
      var username = document.getElementById('username').value;
      var password = document.getElementById('password').value;
      Auth.login(username, password).then(function(data) {
        if (data.user.mustChangePassword) {
          window.location.href = rel('changepassword.html');
        } else {
          window.location.href = rel('dashboard/dashboard.html');
        }
      }).catch(function(err) {
        showToast(err.message, 'error');
        btn.disabled = false;
      });
    });
  });

  document.addEventListener('click', function(e) {
    var el = e.target;
    if (el.tagName === 'A' || el.tagName === 'SPAN' || el.tagName === 'svg' || el.tagName === 'path') {
      var link = el.closest('a');
      if (link && link.closest('.sidebar-ftr') && link.getAttribute('href').indexOf('login') !== -1) {
        e.preventDefault();
        Auth.logout();
      }
    }
  });
})();
