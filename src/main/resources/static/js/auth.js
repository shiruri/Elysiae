(function() {
  'use strict';

  function rootPath() {
    var depth = location.pathname.replace(/\/$/,'').split('/').length - 2;
    return depth > 0 ? '../'.repeat(depth) : './';
  }
  var toRoot = rootPath();
  var toLogin = toRoot + 'login.html';
  var toDashboard = toRoot + 'dashboard/dashboard.html';

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

  var publicPages = ['login', 'index', ''];

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
      window.location.href = toLogin;
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
      if (getPage() === 'login' && Auth.isLoggedIn()) {
        var params = new URLSearchParams(location.search);
        if (!params.get('changePassword')) {
          window.location.href = toDashboard;
        }
      }
      return;
    }
    if (!Auth.isLoggedIn()) {
      window.location.href = toLogin;
      return;
    }
    var user = Auth.getUser();
    if (user && user.mustChangePassword) {
      window.location.href = toLogin + '?changePassword=1';
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

    var params = new URLSearchParams(location.search);
    if (params.get('changePassword')) {
      var oldIdx = -1, newIdx = -1, btnIdx = -1;
      var children = form.querySelectorAll('input, button');
      for (var i = 0; i < children.length; i++) {
        if (children[i].id === 'password') oldIdx = i;
      }
      form.innerHTML =
        '<div class="flex flex-col gap-4">' +
        '<div class="alert alert-warning" style="padding:.55rem .85rem;border-radius:8px;font-size:.8125rem;display:flex;align-items:center;gap:.5rem;background:#fffbeb;color:#b45309">Your password must be changed before continuing.</div>' +
        '<div class="form-group"><label class="form-label" for="cp-old">Current Password</label><input type="password" id="cp-old" class="form-input" required></div>' +
        '<div class="form-group"><label class="form-label" for="cp-new">New Password</label><input type="password" id="cp-new" class="form-input" required minlength="6"></div>' +
        '<button type="submit" class="w-full mt-1 inline-flex items-center justify-center gap-2 px-6 py-3 rounded-lg text-base font-medium text-white bg-primary-600 hover:bg-primary-700 transition-colors shadow-sm" id="cp-btn">Change Password</button>' +
        '</div>';

      form.addEventListener('submit', function(e) {
        e.preventDefault();
        var oldPw = document.getElementById('cp-old').value;
        var newPw = document.getElementById('cp-new').value;
        var newBtn = document.getElementById('cp-btn');
        newBtn.disabled = true;
        Auth.changePassword(oldPw, newPw).then(function() {
          showToast('Password changed successfully! Redirecting...', 'success');
          setTimeout(function() { window.location.href = toDashboard; }, 1000);
        }).catch(function(err) {
          showToast(err.message, 'error');
          newBtn.disabled = false;
        });
      });
      return;
    }

    form.addEventListener('submit', function(e) {
      e.preventDefault();
      btn.disabled = true;
      var username = document.getElementById('username').value;
      var password = document.getElementById('password').value;
      Auth.login(username, password).then(function(data) {
        if (data.user.mustChangePassword) {
          window.location.href = toLogin + '?changePassword=1';
        } else {
          window.location.href = toDashboard;
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
