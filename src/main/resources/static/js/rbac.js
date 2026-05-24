(function() {
  'use strict';

  var MODULE_ACCESS = {
    dashboard:      ['ADMIN','DOCTOR','NURSE','RECEPTIONIST','LAB_TECH','PHARMACIST','CASHIER','PATIENT'],
    appointments:   ['ADMIN','DOCTOR','NURSE','RECEPTIONIST','PATIENT'],
    patients:       ['ADMIN','DOCTOR','NURSE','RECEPTIONIST'],
    doctors:        ['ADMIN','DOCTOR','RECEPTIONIST','PATIENT','NURSE'],
    records:        ['ADMIN','DOCTOR','NURSE'],
    lab:            ['ADMIN','DOCTOR','LAB_TECH','NURSE'],
    pharmacy:       ['ADMIN','PHARMACIST','DOCTOR'],
    admissions:     ['ADMIN','DOCTOR','NURSE','RECEPTIONIST'],
    wards:          ['ADMIN','RECEPTIONIST','DOCTOR','NURSE'],
    departments:    ['ADMIN','DOCTOR','RECEPTIONIST','PATIENT','NURSE'],
    billing:        ['ADMIN','CASHIER','PATIENT'],
    reports:        ['ADMIN'],
    staff:          ['ADMIN'],
    settings:       ['ADMIN','DOCTOR','NURSE','RECEPTIONIST','LAB_TECH','PHARMACIST','CASHIER','PATIENT'],
    profile:        ['ADMIN','DOCTOR','NURSE','RECEPTIONIST','LAB_TECH','PHARMACIST','CASHIER','PATIENT'],
    'service-rate': ['ADMIN'],
    audit:          ['ADMIN']
  };

  var PAGE_MODULE = {
    dashboard: 'dashboard', appointments: 'appointments', patients: 'patients',
    doctors: 'doctors', records: 'records', lab: 'lab', pharmacy: 'pharmacy', admissions: 'admissions',
    wards: 'wards', departments: 'departments', billing: 'billing',
    reports: 'reports', staff: 'staff', settings: 'settings', profile: 'profile',
    'service-rate': 'service-rate', audit: 'audit'
  };

  function getRole() { return Auth.getRole(); }

  function hasAccess(module) {
    var role = getRole();
    if (!role) return false;
    var allowed = MODULE_ACCESS[module];
    return allowed && allowed.indexOf(role) !== -1;
  }

  function gatePage() {
    var page = location.pathname.split('/').filter(Boolean).pop().replace('.html','') || '';
    var module = PAGE_MODULE[page];
    if (module && !hasAccess(module)) {
      window.location.href = 'dashboard/dashboard.html';
    }
  }

  function gateSidebar() {
    var links = document.querySelectorAll('.sidebar .nav-item[href]');
    for (var i = 0; i < links.length; i++) {
      var a = links[i];
      var href = a.getAttribute('href');
      var parts = href.split('/').filter(Boolean);
      var file = parts.pop().replace('.html','');
      var module = PAGE_MODULE[file];
      if (module && !hasAccess(module)) {
        a.style.display = 'none';
      }
    }

    var bottomItems = document.querySelectorAll('.bottom-nav-item[href], .bottom-nav-more .nav-item[href]');
    for (var j = 0; j < bottomItems.length; j++) {
      var b = bottomItems[j];
      var href2 = b.getAttribute('href');
      if (!href2) continue;
      var parts2 = href2.split('/').filter(Boolean);
      var file2 = parts2.pop().replace('.html','');
      var module2 = PAGE_MODULE[file2];
      if (module2 && !hasAccess(module2)) {
        b.style.display = 'none';
      }
    }
  }

  function updateUserBadge() {
    var user = Auth.getUser();
    if (!user) return;
    var avatar = document.querySelector('.avatar');
    var nameEl = document.querySelector('.topbar .leading-tight div:first-child');
    var roleEl = document.querySelector('.topbar .leading-tight div:last-child');
    if (avatar) avatar.textContent = (user.username || 'U').substring(0, 2).toUpperCase();
    if (nameEl) nameEl.textContent = user.username || 'User';
    if (roleEl) {
      var r = user.role || '';
      roleEl.textContent = r.charAt(0) + r.slice(1).toLowerCase();
    }
  }

  function init() {
    if (!Auth.isLoggedIn()) return;
    gatePage();
    gateSidebar();
    updateUserBadge();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
