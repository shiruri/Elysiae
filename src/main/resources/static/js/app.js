(function () {
  'use strict';

  var page = location.pathname.split('/').filter(Boolean).pop()?.replace('.html','');
  var body = document.body;
  var sb = document.getElementById('sidebar');
  var ov = document.getElementById('sidebar-overlay');
  var tog = document.getElementById('sidebar-toggle');
  /* ── sidebar toggle (mobile) ── */
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

  /* ── demo toast ── */
  setTimeout(function () { window.showToast('Page loaded successfully', 'success'); }, 600);

  /* ── confirm helper ── */
  window.confirmAction = function (msg, fn) {
    if (window.confirm(msg || 'Are you sure?')) fn();
  };

  /* ── ACCESSIBILITY ── */
  var a11yKey = 'elysiae-a11y';
  var a11yDefaults = {
    font: 'md', highContrast: false, reduceMotion: false, dyslexia: false,
    fontWeight: false, lineHeight: false, letterSpacing: false,
    highlightLinks: false, highlightTitles: false,
    readingGuide: false, bigCursor: false, superFocus: false,
    monochrome: false, darkContrast: false, lightContrast: false, lowSaturation: false,
    hideImages: false, activeProfile: null
  };
  var a11ySettings = {};

  function loadA11y() {
    try {
      var stored = JSON.parse(localStorage.getItem(a11yKey));
      a11ySettings = stored || {};
    } catch(e) { a11ySettings = {}; }
    Object.keys(a11yDefaults).forEach(function (k) {
      if (a11ySettings[k] === undefined) a11ySettings[k] = a11yDefaults[k];
    });
  }

  function saveA11y() {
    localStorage.setItem(a11yKey, JSON.stringify(a11ySettings));
  }

  function camelToHyphen(s) {
    return s.replace(/([A-Z])/g, '-$1').toLowerCase();
  }

  function applyA11y() {
    var html = document.documentElement;
    var toggleKeys = ['highContrast','reduceMotion','dyslexia','fontWeight','lineHeight','letterSpacing',
      'highlightLinks','highlightTitles','readingGuide','bigCursor','superFocus',
      'monochrome','darkContrast','lightContrast','lowSaturation','hideImages'];
    ['font-sm','font-lg','font-xl'].forEach(function (c) { html.classList.remove('a11y-' + c); });
    toggleKeys.forEach(function (k) {
      html.classList.toggle('a11y-' + camelToHyphen(k), !!a11ySettings[k]);
    });
    var f = a11ySettings.font;
    if (f && f !== 'md') html.classList.add('a11y-font-' + f);
    syncA11yUI();
    updateReadingGuide();
  }

  function syncA11yUI() {
    document.querySelectorAll('.settings-pill, .a11y-widget-pill').forEach(function (b) {
      b.classList.toggle('active', b.dataset.value === a11ySettings.font);
    });
    document.querySelectorAll('.settings-option').forEach(function (opt) {
      var key = opt.dataset.a11y;
      if (!key) return;
      var on = !!a11ySettings[key];
      opt.querySelector('.settings-toggle').classList.toggle('on', on);
      opt.querySelector('.settings-toggle').setAttribute('aria-checked', on);
    });
    document.querySelectorAll('.a11y-widget-row[data-a11y]').forEach(function (row) {
      var key = row.dataset.a11y;
      var on = !!a11ySettings[key];
      row.querySelector('.a11y-widget-toggle').classList.toggle('on', on);
    });
    document.querySelectorAll('.a11y-profile-btn').forEach(function (btn) {
      btn.classList.toggle('active', btn.dataset.profile === a11ySettings.activeProfile);
    });
  }

  function announceA11y(msg) {
    var live = document.getElementById('a11y-live');
    if (live) { live.textContent = msg; }
  }

  loadA11y();
  applyA11y();

  /* ── settings panel toggle ── */
  document.addEventListener('click', function (e) {
    var navItem = e.target.closest('#nav-settings');
    var overlay = document.getElementById('settings-overlay');
    var panel = document.getElementById('settings-panel');
    if (!overlay || !panel) return;
    if (navItem) {
      e.preventDefault();
      overlay.classList.toggle('open');
      panel.focus();
      return;
    }
    if (e.target.closest('#settings-close') || (overlay.classList.contains('open') && !e.target.closest('.settings-panel'))) {
      overlay.classList.remove('open');
    }
  });

  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
      var overlay = document.getElementById('settings-overlay');
      if (overlay && overlay.classList.contains('open')) overlay.classList.remove('open');
    }
  });

  /* ── font size pills ── */
  document.addEventListener('click', function (e) {
    var pill = e.target.closest('.settings-pill');
    if (!pill) return;
    a11ySettings.font = pill.dataset.value;
    saveA11y();
    applyA11y();
    announceA11y('Font size changed to ' + pill.textContent.trim());
  });

  /* ── toggle switches ── */
  document.addEventListener('click', function (e) {
    var toggle = e.target.closest('.settings-toggle');
    if (!toggle) return;
    var opt = toggle.closest('.settings-option');
    if (!opt) return;
    var key = opt.dataset.a11y;
    if (!key) return;
    a11ySettings[key] = !a11ySettings[key];
    saveA11y();
    applyA11y();
    announceA11y((a11ySettings[key] ? 'Enabled' : 'Disabled') + ' ' + key.replace(/([A-Z])/g, ' $1').toLowerCase());
  });

  /* ── reset all ── */
  document.addEventListener('click', function (e) {
    if (!e.target.closest('#settings-reset')) return;
    Object.keys(a11yDefaults).forEach(function (k) { a11ySettings[k] = a11yDefaults[k]; });
    saveA11y();
    applyA11y();
    announceA11y('Accessibility settings reset');
    window.showToast('Accessibility settings reset to default', 'success');
  });

  /* ── PAGE ENTER ── */
  var mainWrap = document.querySelector('.main-wrap');
  if (mainWrap && !mainWrap.classList.contains('page-enter')) {
    mainWrap.classList.add('page-enter');
  }

  /* ── SIDEBAR POSITION ── */
  var positions = ['left', 'right', 'bottom'];
  var posLabels = { left: 'Left sidebar', right: 'Right sidebar', bottom: 'Bottom bar' };
  var posIcons = {
    left: '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="9" y1="3" x2="9" y2="21"/></svg>',
    right: '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="15" y1="3" x2="15" y2="21"/></svg>',
    bottom: '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="3" y1="15" x2="21" y2="15"/></svg>'
  };

  function getPosition() {
    if (window.innerWidth <= 768) return 'bottom';
    return localStorage.getItem('elysiae-sidebar-pos') || 'left';
  }

  function setPosition(pos) {
    body.classList.add('sidebar-pos-transitioning');
    positions.forEach(function (p) {
      sb.classList.remove('pos-' + p);
      body.classList.remove('sidebar-pos-' + p);
    });
    if (pos !== 'left') {
      sb.classList.add('pos-' + pos);
      body.classList.add('sidebar-pos-' + pos);
    }
    localStorage.setItem('elysiae-sidebar-pos', pos);
    requestAnimationFrame(function () {
      requestAnimationFrame(function () {
        body.classList.remove('sidebar-pos-transitioning');
      });
    });
  }

  if (sb) setPosition(getPosition());

  /* ── DRAG TO REPOSITION (via grip handle) ── */
  var posGrip = sb ? sb.querySelector('.sidebar-pos-grip') : null;
  var posDragging = false;
  var ghostEl = null;

  function createGhost() {
    if (!ghostEl) {
      ghostEl = document.createElement('div');
      ghostEl.className = 'sidebar-pos-ghost hidden';
      ghostEl.textContent = 'Drop here';
      document.body.appendChild(ghostEl);
    }
    return ghostEl;
  }

  function getNearestPosition(x, y, vw, vh) {
    if (y > vh * 0.65) return 'bottom';
    if (x < vw * 0.25) return 'left';
    if (x > vw * 0.75) return 'right';
    return null;
  }

  function showGhost(pos, vw, vh) {
    var g = createGhost();
    g.className = 'sidebar-pos-ghost';
    g.classList.remove('hidden');
    g.style.cssText = '';
    g.style.position = 'fixed';
    g.setAttribute('data-label', posLabels[pos]);
    if (pos === 'left') {
      g.style.top = '0'; g.style.left = '0'; g.style.bottom = '0';
      g.style.width = Math.round(vw * 0.2) + 'px';
    } else if (pos === 'right') {
      g.style.top = '0'; g.style.right = '0'; g.style.bottom = '0';
      g.style.width = Math.round(vw * 0.2) + 'px';
    } else if (pos === 'bottom') {
      g.style.left = '0'; g.style.right = '0'; g.style.bottom = '0';
      g.style.height = '72px';
    }
  }

  function hideGhost() {
    if (ghostEl) ghostEl.classList.add('hidden');
  }

  function onPosDragStart(e) {
    if (e.type === 'mousedown' && e.button !== 0) return;
    posDragging = true;
    body.classList.add('sidebar-pos-dragging');
    document.addEventListener('mousemove', onPosDrag);
    document.addEventListener('mouseup', onPosDragEnd);
    document.addEventListener('touchmove', onPosDrag, { passive: true });
    document.addEventListener('touchend', onPosDragEnd);
    e.preventDefault();
  }

  function onPosDrag(e) {
    if (!posDragging) return;
    var cx = e.type === 'touchmove' ? e.touches[0].clientX : e.clientX;
    var cy = e.type === 'touchmove' ? e.touches[0].clientY : e.clientY;
    var pos = getNearestPosition(cx, cy, window.innerWidth, window.innerHeight);
    if (pos) showGhost(pos, window.innerWidth, window.innerHeight);
    else hideGhost();
  }

  function onPosDragEnd(e) {
    posDragging = false;
    body.classList.remove('sidebar-pos-dragging');
    document.removeEventListener('mousemove', onPosDrag);
    document.removeEventListener('mouseup', onPosDragEnd);
    document.removeEventListener('touchmove', onPosDrag);
    document.removeEventListener('touchend', onPosDragEnd);
    var cx = e.type === 'touchend' ? e.changedTouches[0].clientX : e.clientX;
    var cy = e.type === 'touchend' ? e.changedTouches[0].clientY : e.clientY;
    var pos = getNearestPosition(cx, cy, window.innerWidth, window.innerHeight);
    hideGhost();
    if (pos && pos !== getPosition()) {
      setPosition(pos);
      window.showToast(posLabels[pos], 'success');
    }
  }

  if (posGrip && sb) {
    function guardedDragStart(e) {
      if (window.innerWidth <= 768) return;
      onPosDragStart(e);
    }
    posGrip.addEventListener('mousedown', guardedDragStart);
    posGrip.addEventListener('touchstart', guardedDragStart, { passive: false });
  }

  /* ── RIGHT-CLICK CONTEXT MENU (on bottom nav) ── */
  var ctxMenu = document.getElementById('pos-context-menu');

  function buildContextMenu() {
    if (!ctxMenu) {
      ctxMenu = document.createElement('div');
      ctxMenu.id = 'pos-context-menu';
      ctxMenu.className = 'pos-context-menu';
      document.body.appendChild(ctxMenu);
    }
    var cur = getPosition();
    ctxMenu.innerHTML = '';
    positions.forEach(function (p) {
      var btn = document.createElement('button');
      btn.className = 'pos-context-item' + (p === cur ? ' active' : '');
      btn.dataset.pos = p;
      btn.innerHTML = posIcons[p] + '<span>' + posLabels[p] + '</span>' +
        '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>';
      btn.addEventListener('click', function () {
        setPosition(p);
        hideContextMenu();
        window.showToast(posLabels[p], 'success');
      });
      ctxMenu.appendChild(btn);
    });
  }

  function showContextMenu(e) {
    buildContextMenu();
    ctxMenu.classList.add('open');
    var x = e.clientX, y = e.clientY;
    var mw = ctxMenu.offsetWidth, mh = ctxMenu.offsetHeight;
    if (x + mw > window.innerWidth) x = window.innerWidth - mw - 8;
    if (y + mh > window.innerHeight) y = window.innerHeight - mh - 8;
    ctxMenu.style.left = x + 'px';
    ctxMenu.style.top = y + 'px';
  }

  function hideContextMenu() {
    if (ctxMenu) ctxMenu.classList.remove('open');
  }

  document.addEventListener('contextmenu', function (e) {
    if (e.target.closest('.bottom-nav') || e.target.closest('.sidebar.pos-bottom')) {
      e.preventDefault();
      showContextMenu(e);
    }
  });

  document.addEventListener('click', function (e) {
    if (ctxMenu && !ctxMenu.contains(e.target)) hideContextMenu();
  });

  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') hideContextMenu();
  });

  /* ── sidebar drag to resize (desktop) ── */
  var dragHandle = document.querySelector('.sidebar-drag');
  var startX, startW;

  if (dragHandle && sb) {
    function onResizeStart(e) {
      if (sb.classList.contains('pos-bottom')) return;
      startX = e.type === 'mousedown' ? e.clientX : e.touches[0].clientX;
      startW = sb.offsetWidth;
      body.classList.add('sidebar-dragging');
      document.addEventListener('mousemove', onResize);
      document.addEventListener('mouseup', onResizeEnd);
      document.addEventListener('touchmove', onResize, { passive: true });
      document.addEventListener('touchend', onResizeEnd);
      e.preventDefault();
    }
    function onResize(e) {
      var x = e.type === 'mousemove' ? e.clientX : e.touches[0].clientX;
      var dx = x - startX;
      if (sb.classList.contains('pos-right')) dx = -dx;
      var w = Math.max(52, Math.min(360, startW + dx));
      sb.style.width = w + 'px';
      sb.classList.add('open');
    }
    function onResizeEnd() {
      body.classList.remove('sidebar-dragging');
      document.removeEventListener('mousemove', onResize);
      document.removeEventListener('mouseup', onResizeEnd);
      document.removeEventListener('touchmove', onResize);
      document.removeEventListener('touchend', onResizeEnd);
      if (sb) {
        var w = parseInt(sb.style.width);
        if (w < 90) { sb.style.width = ''; sb.classList.remove('open'); }
        else { sb.style.width = ''; }
      }
    }
    dragHandle.addEventListener('mousedown', onResizeStart);
    dragHandle.addEventListener('touchstart', onResizeStart, { passive: false });
  }

  /* ── mobile swipe to open sidebar ── */
  var swipeIndicator = document.querySelector('.swipe-indicator');
  var swipeStartX = 0;
  var swiping = false;

  document.addEventListener('touchstart', function (e) {
    if (window.innerWidth > 768) return;
    swipeStartX = e.touches[0].clientX;
    swiping = swipeStartX < 20;
  }, { passive: true });

  document.addEventListener('touchmove', function (e) {
    if (!swiping || !sb) return;
    var x = e.touches[0].clientX;
    if (sb.classList.contains('open')) return;
    if (x > 30 && x < 120) {
      sb.style.transform = 'translateX(' + Math.max(-200, -(200 - x * 1.5)) + 'px)';
      sb.style.transition = 'none';
      if (swipeIndicator) swipeIndicator.classList.add('show');
    }
  }, { passive: true });

  document.addEventListener('touchend', function (e) {
    if (!swiping || !sb) return;
    sb.style.transition = '';
    sb.style.transform = '';
    if (swipeIndicator) swipeIndicator.classList.remove('show');
    var x = e.changedTouches[0].clientX;
    if (x > 80) {
      sb.classList.add('open');
      if (ov) ov.classList.add('active');
    }
    swiping = false;
  }, { passive: true });

  /* ── bottom nav ── */
  var bottomNav = document.querySelector('.bottom-nav');
  if (bottomNav) {
    setTimeout(function () { bottomNav.classList.add('active'); }, 100);
  }

  document.querySelectorAll('.bottom-nav-item[href]').forEach(function (a) {
    var href = a.getAttribute('href').split('/').filter(Boolean).pop()?.replace('.html','');
    if (page === href) a.classList.add('active');
  });

  var moreBtn = document.getElementById('bottom-more-btn');
  var moreMenu = document.getElementById('bottom-more-menu');
  if (moreBtn && moreMenu) {
    moreBtn.addEventListener('click', function (e) {
      e.stopPropagation();
      moreMenu.classList.toggle('open');
    });
    document.addEventListener('click', function () {
      moreMenu.classList.remove('open');
    });
  }

  /* ── sidebar nav active link (safe, href-only) ── */
  document.querySelectorAll('.sidebar .nav-item[href]').forEach(function (a) {
    var href = a.getAttribute('href').split('/').filter(Boolean).pop()?.replace('.html','');
    if (page === href || (!page && href === 'dashboard')) a.classList.add('active');
  });

  /* ── READING GUIDE ── */
  var guideEl = null;
  function updateReadingGuide() {
    if (!guideEl) {
      guideEl = document.createElement('div');
      guideEl.className = 'reading-guide';
      document.body.appendChild(guideEl);
    }
    guideEl.classList.toggle('active', !!a11ySettings.readingGuide);
  }
  document.addEventListener('mousemove', function (e) {
    if (guideEl && guideEl.classList.contains('active')) {
      guideEl.style.top = (e.clientY - 1) + 'px';
    }
  });

  /* ── ACCESSIBILITY WIDGET ── */
  var widgetBuilt = false;
  function buildWidget() {
    if (widgetBuilt) return;
    widgetBuilt = true;

    var btn = document.createElement('button');
    btn.className = 'a11y-widget-btn';
    btn.setAttribute('aria-label', 'Accessibility tools');
    btn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 8h.01"/><path d="M12 12v4"/></svg>';
    document.body.appendChild(btn);

    var panel = document.createElement('div');
    panel.className = 'a11y-widget-panel';
    panel.setAttribute('role', 'dialog');
    panel.setAttribute('aria-label', 'Accessibility tools');

    var closeSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>';

    function sTog(key, label, desc) {
      return '<div class="a11y-widget-row" data-a11y="'+key+'"><div><div class="a11y-widget-row-label">'+label+'</div>'+(desc?'<div class="a11y-widget-row-desc">'+desc+'</div>':'')+'</div><div class="a11y-widget-toggle'+(a11ySettings[key]?' on':'')+'"></div></div>';
    }

    var profiles = [
      { id:'seizure', label:'Seizure Safe', icon:'<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>' },
      { id:'low-vision', label:'Low Vision', icon:'<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/></svg>' },
      { id:'adhd', label:'ADHD Focus', icon:'<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>' },
      { id:'dyslexia', label:'Dyslexia', icon:'<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="4 7 4 4 20 4 20 7"/><line x1="9" y1="20" x2="15" y2="20"/><line x1="12" y1="4" x2="12" y2="20"/></svg>' },
      { id:'motor', label:'Motor Impaired', icon:'<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="2" width="20" height="20" rx="2" ry="2"/><line x1="2" y1="12" x2="22" y2="12"/></svg>' }
    ];

    panel.innerHTML =
      '<div class="a11y-widget-hdr"><h3><svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 8h.01"/><path d="M12 12v4"/></svg> Accessibility</h3><button class="a11y-widget-close" aria-label="Close">'+closeSvg+'</button></div>' +
      '<div class="a11y-widget-body">' +
        '<div class="a11y-widget-section"><div class="a11y-widget-section-title">Profiles</div><div class="a11y-widget-profiles">' +
          profiles.map(function(p){return '<button class="a11y-profile-btn'+(a11ySettings.activeProfile===p.id?' active':'')+'" data-profile="'+p.id+'">'+p.icon+'<span>'+p.label+'</span></button>'}).join('') +
        '</div></div>' +
        '<div class="a11y-widget-section"><div class="a11y-widget-section-title">Content</div>' +
          '<div class="a11y-widget-row"><div><div class="a11y-widget-row-label">Font size</div></div><div class="a11y-widget-pills">' +
            ['sm','md','lg','xl'].map(function(v){return '<button class="a11y-widget-pill'+(a11ySettings.font===v?' active':'')+'" data-value="'+v+'">'+(v==='sm'?'A-':v==='md'?'A':v==='lg'?'A+':'A++')+'</button>'}).join('') +
          '</div></div>' +
          sTog('dyslexia','Dyslexia font','Lexend typeface for readability') +
          sTog('fontWeight','Bold text') +
          sTog('lineHeight','Line height','Increase line spacing') +
          sTog('letterSpacing','Letter spacing','Increase character spacing') +
          sTog('highlightLinks','Highlight links') +
          sTog('highlightTitles','Highlight headings') +
        '</div>' +
        '<div class="a11y-widget-section"><div class="a11y-widget-section-title">Visual</div>' +
          sTog('readingGuide','Reading guide','Horizontal line to follow text') +
          sTog('bigCursor','Big cursor','Larger cursor with target ring') +
          sTog('superFocus','Super focus','Dim everything except focused element') +
        '</div>' +
        '<div class="a11y-widget-section"><div class="a11y-widget-section-title">Color</div>' +
          sTog('highContrast','High contrast') +
          sTog('monochrome','Monochrome','Remove all color') +
          sTog('darkContrast','Dark contrast','Invert colors') +
          sTog('lightContrast','Light contrast','Revert color inversion') +
          sTog('lowSaturation','Low saturation','Reduce color intensity') +
        '</div>' +
        '<div class="a11y-widget-section"><div class="a11y-widget-section-title">Tools</div>' +
          sTog('reduceMotion','Stop animations','Disable all animations') +
          sTog('hideImages','Hide images','Text-only browsing') +
        '</div>' +
        '<button class="btn btn-secondary btn-sm a11y-widget-reset" id="widget-settings-reset">Reset all</button>' +
      '</div>';

    document.body.appendChild(panel);

    /* toggle panel */
    btn.addEventListener('click', function () {
      panel.classList.toggle('open');
    });
    panel.querySelector('.a11y-widget-close').addEventListener('click', function () {
      panel.classList.remove('open');
    });
    document.addEventListener('click', function (e) {
      if (panel.classList.contains('open') && !panel.contains(e.target) && !btn.contains(e.target)) {
        panel.classList.remove('open');
      }
    });
    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape' && panel.classList.contains('open')) panel.classList.remove('open');
    });

    /* profile clicks */
    panel.addEventListener('click', function (e) {
      var profBtn = e.target.closest('.a11y-profile-btn');
      if (!profBtn) return;
      var pid = profBtn.dataset.profile;
      if (a11ySettings.activeProfile === pid) {
        a11ySettings.activeProfile = null;
      } else {
        a11ySettings.activeProfile = pid;
        var presets = {
          seizure: { reduceMotion: true, monochrome: true },
          'low-vision': { highContrast: true, font: 'lg', bigCursor: true },
          adhd: { readingGuide: true, reduceMotion: true, superFocus: true },
          dyslexia: { dyslexia: true, lineHeight: true, letterSpacing: true, fontWeight: true },
          motor: { bigCursor: true, reduceMotion: true }
        };
        var vals = presets[pid];
        if (vals) {
          Object.keys(a11yDefaults).forEach(function (k) {
            if (k === 'activeProfile' || k === 'font') return;
            a11ySettings[k] = (k in vals) ? vals[k] : a11yDefaults[k];
          });
          a11ySettings.font = vals.font || a11yDefaults.font;
        }
      }
      saveA11y();
      applyA11y();
      announceA11y(profBtn.textContent.trim() + ' profile ' + (a11ySettings.activeProfile ? 'activated' : 'deactivated'));
    });

    /* font size pills */
    panel.addEventListener('click', function (e) {
      var pill = e.target.closest('.a11y-widget-pill');
      if (!pill) return;
      a11ySettings.font = pill.dataset.value;
      a11ySettings.activeProfile = null;
      saveA11y();
      applyA11y();
      announceA11y('Font size changed');
    });

    /* toggle switches */
    panel.addEventListener('click', function (e) {
      var toggle = e.target.closest('.a11y-widget-toggle');
      if (!toggle) return;
      var row = toggle.closest('.a11y-widget-row');
      if (!row) return;
      var key = row.dataset.a11y;
      if (!key) return;
      a11ySettings[key] = !a11ySettings[key];
      a11ySettings.activeProfile = null;
      saveA11y();
      applyA11y();
      announceA11y((a11ySettings[key] ? 'Enabled: ' : 'Disabled: ') + row.querySelector('.a11y-widget-row-label').textContent.trim());
    });

    /* reset */
    panel.addEventListener('click', function (e) {
      if (!e.target.closest('#widget-settings-reset')) return;
      Object.keys(a11yDefaults).forEach(function (k) { a11ySettings[k] = a11yDefaults[k]; });
      saveA11y();
      applyA11y();
      announceA11y('All accessibility settings reset');
      window.showToast('Accessibility settings reset', 'success');
    });
  }

  /* build widget on idle/ready */
  if (document.readyState === 'complete') buildWidget();
  else window.addEventListener('load', function () { setTimeout(buildWidget, 600); });

})();
