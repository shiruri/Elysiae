(function () {
  'use strict';

  /* ── sidebar active link ── */
  var page = location.pathname.split('/').filter(Boolean).pop()?.replace('.html','');
  document.querySelectorAll('.sidebar .nav-item').forEach(function (a) {
    var href = a.getAttribute('href').split('/').filter(Boolean).pop()?.replace('.html','');
    if (page === href || (!page && href === 'dashboard')) a.classList.add('active');
  });

  /* ── sidebar toggle (mobile) ── */
  var tog = document.getElementById('sidebar-toggle');
  var sb = document.getElementById('sidebar');
  var ov = document.getElementById('sidebar-overlay');
  if (tog && sb) {
    tog.addEventListener('click', function () {
      sb.classList.toggle('open');
      if (ov) ov.classList.toggle('active');
    });
  }
  if (ov && sb) {
    ov.addEventListener('click', function () {
      sb.classList.remove('open');
      ov.classList.remove('active');
    });
  }

  /* ── close sidebar on Escape ── */
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape' && sb && sb.classList.contains('open')) {
      sb.classList.remove('open');
      if (ov) ov.classList.remove('active');
    }
  });

  /* ── loader ── */
  function hideLoader() {
    var el = document.getElementById('loader');
    if (el) el.classList.add('hidden');
  }
  if (document.readyState === 'complete') hideLoader();
  else window.addEventListener('load', function () { setTimeout(hideLoader, 300); });

  /* ── toast system ── */
  window.showToast = function (message, type) {
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
    t.innerHTML = (icons[type] || icons.info) + '<span>' + message + '</span>';
    ctn.appendChild(t);
    setTimeout(function () {
      t.classList.add('hiding');
      setTimeout(function () { if (t.parentNode) t.parentNode.removeChild(t); }, 250);
    }, 3500);
  };

  /* ── demo toasts on page load ── */
  setTimeout(function () {
    window.showToast('Page loaded successfully', 'success');
  }, 600);

  /* ── confirm action helper ── */
  window.confirmAction = function (msg, fn) {
    if (window.confirm(msg || 'Are you sure?')) fn();
  };

})();
