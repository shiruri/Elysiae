(function() {
  'use strict';

  if (!Auth.isLoggedIn()) return;

  var page = location.pathname.split('/').filter(Boolean).pop().replace('.html','') || '';

  var state = { page: 0, size: 25, query: '', total: 0 };
  var _reload = null;

  window.onModalSuccess = function(id) {
    if (typeof _reload === 'function') _reload();
  };

  function esc(s) { return String(s||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;'); }

  function loading(el) {
    if(el) el.innerHTML = '<tr><td colspan="10" class="text-center py-12"><div class="loader-spin" style="margin:0 auto"></div></td></tr>';
  }

  function empty(el, msg, action) {
    if(!el) return;
    el.innerHTML = '<tr><td colspan="10" class="text-center py-16"><div class="empty-state">' +
      '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"/><polyline points="13 2 13 9 20 9"/></svg>' +
      '<p>' + (msg||'No data') + '</p>' + (action||'') + '</div></td></tr>';
  }

  function pagination(ctn, total, page, size, cb) {
    if(!ctn) return;
    var pages = Math.ceil(total / size);
    if(pages <= 1) { ctn.innerHTML = ''; return; }
    var h = '<div class="pagination">';
    h += '<button class="pg-btn" data-p="'+(page-1)+'" '+(page<=0?'disabled':'')+'><svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/></svg></button>';
    for(var i=0;i<pages;i++){
      h += '<button class="pg-btn'+(i===page?' active':'')+'" data-p="'+i+'">'+(i+1)+'</button>';
    }
    h += '<button class="pg-btn" data-p="'+(page+1)+'" '+(page>=pages-1?'disabled':'')+'><svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg></button>';
    h += '</div>';
    ctn.innerHTML = h;
    ctn.querySelectorAll('.pg-btn:not([disabled])').forEach(function(b){
      b.addEventListener('click', function(){ cb(parseInt(this.dataset.p)); });
    });
  }

  /* ── Dashboard ── */
  if(page === 'dashboard') {
    API.get('/api/reports/dashboard').then(function(d) {
      var cards = document.querySelectorAll('.s-value');
      if(cards.length >= 4) {
        cards[0].textContent = d.totalPatients != null ? d.totalPatients : '--';
        cards[1].textContent = d.appointmentsToday != null ? d.appointmentsToday : '--';
        cards[2].textContent = d.admittedPatients != null ? d.admittedPatients : '--';
        cards[3].textContent = d.revenueToday != null ? '$' + d.revenueToday : '$--';
      }
      var subs = document.querySelectorAll('.s-sub');
      if(subs.length >= 4 && d.metrics) {
        if(d.metrics.patientGrowth) subs[0].textContent = d.metrics.patientGrowth;
        if(d.metrics.apptGrowth) subs[1].textContent = d.metrics.apptGrowth;
        if(d.metrics.admissionGrowth) subs[2].textContent = d.metrics.admissionGrowth;
        if(d.metrics.revenueGrowth) subs[3].textContent = d.metrics.revenueGrowth;
      }
    }).catch(function(err){ showToast(err.message, 'error'); });

    var tblBody = document.querySelector('.tbl table tbody');
    if(tblBody) {
      loading(tblBody);
      API.post('/api/appointments', {page:0,size:5}).then(function(d) {
        var items = d.content || d || [];
        if(!items.length) {
          empty(tblBody, 'No upcoming appointments', '<div class="empty-action"><button class="btn btn-primary btn-sm" onclick="openModal(\'modal-appt-create\')">Schedule first</button></div>');
          return;
        }
        tblBody.innerHTML = items.map(function(a){
          return '<tr><td>' + esc(a.patientName) + '</td><td>' + esc(a.doctorName) + '</td><td>' + esc(a.appointmentDateTime) + '</td><td><span class="badge badge-teal">' + esc(a.status) + '</span></td><td class="text-right"><button class="btn btn-ghost btn-sm" onclick="openModal(\'modal-appt-update\')">Edit</button></td></tr>';
        }).join('');
      }).catch(function(err){ showToast(err.message, 'error'); empty(tblBody, 'Failed to load appointments'); });
    }
  }

  /* ── Patients ── */
  if(page === 'patients') {
    var tblBody = document.querySelector('.tbl table tbody');
    var searchInput = document.querySelector('.filter-bar input');
    var genderSelect = document.querySelector('.filter-bar select');
    var filterBtn = document.querySelector('.filter-bar .btn-primary');
    var paginationCtn = document.querySelector('.tbl') ? (function(){ var d=document.createElement('div'); document.querySelector('.tbl').appendChild(d); return d; })() : null;

    function loadPatients() {
      if(!tblBody) return;
      loading(tblBody);
      var body = {page:state.page, size:state.size};
      if(state.query) body.keyword = state.query;
      API.post('/api/patients/get-patients', body).then(function(d) {
        var items = d.content || d || [];
        state.total = d.totalElements || items.length;
        if(!items.length) {
          empty(tblBody, 'No patients found', '<div class="empty-action"><button class="btn btn-primary btn-sm" onclick="openModal(\'modal-patient-create\')">Register first</button></div>');
        } else {
          tblBody.innerHTML = items.map(function(p){
            var age = p.dateOfBirth ? new Date().getFullYear() - new Date(p.dateOfBirth).getFullYear() : '--';
            return '<tr><td><a href="#" class="font-medium text-primary-600 hover:underline">' + esc(p.firstName) + ' ' + esc(p.lastName) + '</a></td><td>' + esc(p.gender) + '</td><td>' + age + '</td><td>' + esc(p.bloodType) + '</td><td>' + esc(p.phone) + '</td><td class="text-right"><div class="flex gap-1 justify-end"><button class="btn btn-ghost btn-sm" onclick="openModal(\'modal-patient-update\')">Edit</button><button class="btn btn-ghost btn-sm text-red-500" onclick="if(confirm(\'Delete patient?\'))API.del(\'/api/patients/'+p.id+'\').then(function(){loadPatients();showToast(\'Deleted\',\'success\');}).catch(function(e){showToast(e.message,\'error\');})">Del</button></div></td></tr>';
          }).join('');
        }
        pagination(paginationCtn, state.total, state.page, state.size, function(p){ state.page=p; loadPatients(); });
      }).catch(function(err){ showToast(err.message, 'error'); empty(tblBody, 'Failed to load patients'); });
    }

    if(filterBtn) {
      filterBtn.addEventListener('click', function(){
        state.query = searchInput ? searchInput.value : '';
        state.page = 0;
        loadPatients();
      });
    }
    if(searchInput) {
      searchInput.addEventListener('keydown', function(e){ if(e.key==='Enter'&&filterBtn)filterBtn.click(); });
    }

    _reload = loadPatients;
    loadPatients();
  }

  /* ── Doctors ── */
  if(page === 'doctors') {
    var tblBody = document.querySelector('.tbl table tbody');
    var searchInput = document.querySelector('.filter-bar input');
    var filterBtn = document.querySelector('.filter-bar .btn-primary');
    var paginationCtn = document.querySelector('.tbl') ? (function(){ var d=document.createElement('div'); document.querySelector('.tbl').appendChild(d); return d; })() : null;

    function loadDoctors() {
      if(!tblBody) return;
      loading(tblBody);
      var body = {page:state.page, size:state.size};
      if(state.query) body.keyword = state.query;
      API.post('/api/doctor', body).then(function(d) {
        var items = d.content || d || [];
        state.total = d.totalElements || items.length;
        if(!items.length) {
          empty(tblBody, 'No doctors found');
        } else {
          tblBody.innerHTML = items.map(function(doc){
            return '<tr><td><span class="font-medium">Dr. ' + esc(doc.firstName) + ' ' + esc(doc.lastName) + '</span></td><td>' + esc(doc.specialization) + '</td><td>' + esc(doc.departmentName) + '</td><td>' + esc(doc.phone) + '</td><td>' + esc(doc.licenseNumber) + '</td><td class="text-right"><div class="flex gap-1 justify-end"><button class="btn btn-ghost btn-sm" onclick="openModal(\'modal-doctor-update\')">Edit</button><button class="btn btn-ghost btn-sm text-red-500" onclick="confirm(\'Delete?\')&&API.del(\'/api/doctor/'+doc.id+'\').then(function(){loadDoctors();showToast(\'Deleted\',\'success\');}).catch(function(e){showToast(e.message,\'error\');})">Del</button></div></td></tr>';
          }).join('');
        }
        pagination(paginationCtn, state.total, state.page, state.size, function(p){ state.page=p; loadDoctors(); });
      }).catch(function(err){ showToast(err.message, 'error'); empty(tblBody, 'Failed to load doctors'); });
    }

    if(filterBtn) {
      filterBtn.addEventListener('click', function(){
        state.query = searchInput ? searchInput.value : '';
        state.page = 0;
        loadDoctors();
      });
    }
    if(searchInput) {
      searchInput.addEventListener('keydown', function(e){ if(e.key==='Enter'&&filterBtn)filterBtn.click(); });
    }

    _reload = loadDoctors;
    loadDoctors();
  }

  /* ── Appointments ── */
  if(page === 'appointments') {
    var tblBody = document.querySelector('.tbl table tbody');
    var searchInput = document.querySelector('.filter-bar input');
    var filterBtn = document.querySelector('.filter-bar .btn-primary');
    var paginationCtn = document.querySelector('.tbl') ? (function(){ var d=document.createElement('div'); document.querySelector('.tbl').appendChild(d); return d; })() : null;

    function loadAppointments() {
      if(!tblBody) return;
      loading(tblBody);
      var body = {page:state.page, size:state.size};
      if(state.query) body.keyword = state.query;
      API.post('/api/appointments', body).then(function(d) {
        var items = d.content || d || [];
        state.total = d.totalElements || items.length;
        var statusMap = {SCHEDULED:'badge-teal',CONFIRMED:'badge-teal',IN_PROGRESS:'badge-amber',COMPLETED:'badge-green',CANCELLED:'badge-red',NO_SHOW:'badge-red'};
        if(!items.length) {
          empty(tblBody, 'No appointments found', '<div class="empty-action"><button class="btn btn-primary btn-sm" onclick="openModal(\'modal-appt-create\')">Create first</button></div>');
        } else {
          tblBody.innerHTML = items.map(function(a){
            var badgeClass = statusMap[a.status] || 'badge-teal';
            return '<tr><td>' + esc(a.patientName) + '</td><td>' + esc(a.doctorName) + '</td><td>' + esc(a.appointmentDateTime) + '</td><td><span class="badge ' + badgeClass + '">' + esc(a.status) + '</span></td><td class="text-right"><div class="flex gap-1 justify-end"><button class="btn btn-ghost btn-sm" onclick="openModal(\'modal-appt-update\')">Edit</button></div></td></tr>';
          }).join('');
        }
        pagination(paginationCtn, state.total, state.page, state.size, function(p){ state.page=p; loadAppointments(); });
      }).catch(function(err){ showToast(err.message, 'error'); empty(tblBody, 'Failed to load appointments'); });
    }
    _reload = loadAppointments;
    loadAppointments();
  }

  /* ── Wards ── */
  if(page === 'wards') {
    var tblBody = document.querySelector('.tbl table tbody');
    var filterBtn = document.querySelector('.filter-bar .btn-primary');
    var paginationCtn = document.querySelector('.tbl') ? (function(){ var d=document.createElement('div'); document.querySelector('.tbl').appendChild(d); return d; })() : null;

    function loadWards() {
      if(!tblBody) return;
      loading(tblBody);
      API.post('/api/wards/search', {page:state.page, size:state.size}).then(function(d) {
        var items = d.content || d || [];
        state.total = d.totalElements || items.length;
        if(!items.length) {
          empty(tblBody, 'No wards found');
        } else {
          tblBody.innerHTML = items.map(function(w){
            return '<tr><td>' + esc(w.name) + '</td><td><span class="badge badge-teal">' + esc(w.type) + '</span></td><td>' + esc(w.floor) + '</td><td>' + (w.availableBeds||'0') + '/' + (w.totalBeds||'0') + '</td><td class="text-right"><div class="flex gap-1 justify-end"><button class="btn btn-ghost btn-sm" onclick="openModal(\'modal-bed-create\')">Add Bed</button></div></td></tr>';
          }).join('');
        }
        pagination(paginationCtn, state.total, state.page, state.size, function(p){ state.page=p; loadWards(); });
      }).catch(function(err){ showToast(err.message, 'error'); empty(tblBody, 'Failed to load wards'); });
    }
    _reload = loadWards;
    loadWards();
  }

  /* ── Billing ── */
  if(page === 'billing') {
    var tblBody = document.querySelector('.tbl table tbody');
    var filterBtn = document.querySelector('.filter-bar .btn-primary');
    var paginationCtn = document.querySelector('.tbl') ? (function(){ var d=document.createElement('div'); document.querySelector('.tbl').appendChild(d); return d; })() : null;

    function loadInvoices() {
      if(!tblBody) return;
      loading(tblBody);
      API.post('/api/billing/search', {page:state.page, size:state.size}).then(function(d) {
        var items = d.content || d || [];
        state.total = d.totalElements || items.length;
        if(!items.length) {
          empty(tblBody, 'No invoices found');
        } else {
          tblBody.innerHTML = items.map(function(inv){
            return '<tr><td><span class="font-medium">#' + inv.id + '</span></td><td>' + esc(inv.patientName) + '</td><td>' + esc(inv.invoiceDate) + '</td><td><span class="font-medium">$' + (inv.totalAmount||'0') + '</span></td><td><span class="badge ' + (inv.status==='PAID'?'badge-green':'badge-amber') + '">' + esc(inv.status) + '</span></td><td class="text-right"><button class="btn btn-ghost btn-sm">View</button></td></tr>';
          }).join('');
        }
        pagination(paginationCtn, state.total, state.page, state.size, function(p){ state.page=p; loadInvoices(); });
      }).catch(function(err){ showToast(err.message, 'error'); empty(tblBody, 'Failed to load invoices'); });
    }
    _reload = loadInvoices;
    loadInvoices();
  }

  /* ── Departments ── */
  if(page === 'departments') {
    var tblBody = document.querySelector('.tbl table tbody');
    var filterBtn = document.querySelector('.filter-bar .btn-primary');
    var paginationCtn = document.querySelector('.tbl') ? (function(){ var d=document.createElement('div'); document.querySelector('.tbl').appendChild(d); return d; })() : null;

    function loadDepartments() {
      if(!tblBody) return;
      loading(tblBody);
      API.get('/api/department').then(function(d) {
        var items = d.content || d || [];
        state.total = d.totalElements || items.length;
        if(!items.length) {
          empty(tblBody, 'No departments found');
        } else {
          tblBody.innerHTML = items.map(function(dept){
            return '<tr><td><span class="font-medium">' + esc(dept.name) + '</span></td><td>' + esc(dept.floor) + '</td><td>' + (dept.doctorCount||'--') + '</td><td class="text-right"><div class="flex gap-1 justify-end"><button class="btn btn-ghost btn-sm" onclick="openModal(\'modal-dept-update\')">Edit</button><button class="btn btn-ghost btn-sm text-red-500" onclick="confirm(\'Delete?\')&&API.del(\'/api/department/'+dept.id+'\').then(function(){loadDepartments();showToast(\'Deleted\',\'success\');}).catch(function(e){showToast(e.message,\'error\');})">Del</button></div></td></tr>';
          }).join('');
        }
        pagination(paginationCtn, state.total, state.page, state.size, function(p){ state.page=p; loadDepartments(); });
      }).catch(function(err){ showToast(err.message, 'error'); empty(tblBody, 'Failed to load departments'); });
    }
    _reload = loadDepartments;
    loadDepartments();
  }

  /* ── Records ── */
  if(page === 'records') {
    var tblBody = document.querySelector('.tbl table tbody');
    var filterBtn = document.querySelector('.filter-bar .btn-primary');
    var searchInput = document.querySelector('.filter-bar input');
    var paginationCtn = document.querySelector('.tbl') ? (function(){ var d=document.createElement('div'); document.querySelector('.tbl').appendChild(d); return d; })() : null;

    function loadRecords() {
      if(!tblBody) return;
      loading(tblBody);
      var body = {page:state.page, size:state.size};
      if(state.query) body.patientName = state.query;
      API.post('/api/patients/get-patients', body).then(function(d) {
        var items = d.content || d || [];
        state.total = d.totalElements || items.length;
        if(!items.length) {
          empty(tblBody, 'No records found');
        } else {
          tblBody.innerHTML = items.map(function(p){
            return '<tr><td><span class="font-medium">' + esc(p.firstName) + ' ' + esc(p.lastName) + '</span></td><td>' + esc(p.gender) + '</td><td>' + esc(p.bloodType||'--') + '</td><td class="text-right"><button class="btn btn-ghost btn-sm" onclick="openModal(\'modal-record-create\')">Add Record</button></td></tr>';
          }).join('');
        }
        pagination(paginationCtn, state.total, state.page, state.size, function(p){ state.page=p; loadRecords(); });
      }).catch(function(err){ showToast(err.message, 'error'); empty(tblBody, 'Failed to load records'); });
    }

    if(filterBtn) {
      filterBtn.addEventListener('click', function(){
        state.query = searchInput ? searchInput.value : '';
        state.page = 0;
        loadRecords();
      });
    }
    _reload = loadRecords;
    loadRecords();
  }

  /* ── Admissions ── */
  if(page === 'admissions') {
    var tblBody = document.querySelector('.tbl table tbody');
    var filterBtn = document.querySelector('.filter-bar .btn-primary');
    var paginationCtn = document.querySelector('.tbl') ? (function(){ var d=document.createElement('div'); document.querySelector('.tbl').appendChild(d); return d; })() : null;

    var currentDoctorId = Auth.getUserId() + '';
    if(!currentDoctorId) currentDoctorId = '1';

    function loadAdmissions() {
      if(!tblBody) return;
      loading(tblBody);
      var role = Auth.getRole();
      var url;
      if(role === 'DOCTOR') url = '/api/admission/' + currentDoctorId + '/doctor';
      else url = '/api/patients/get-patients';
      API.post(url, {page:state.page, size:state.size}).then(function(d) {
        var items = d.content || d || [];
        state.total = d.totalElements || items.length;
        if(!items.length) {
          empty(tblBody, 'No admissions found');
        } else {
          tblBody.innerHTML = items.map(function(a){
            return '<tr><td><span class="font-medium">' + (a.patientName||a.firstName+' '+a.lastName) + '</span></td><td>' + (a.bedNumber||'--') + '</td><td>' + (a.wardName||'--') + '</td><td><span class="badge ' + (a.status==='ACTIVE'?'badge-teal':'badge-gray') + '">' + (a.status||'--') + '</span></td><td class="text-right"><button class="btn btn-ghost btn-sm" onclick="openModal(\'modal-admission-transfer\')">Transfer</button></td></tr>';
          }).join('');
        }
        pagination(paginationCtn, state.total, state.page, state.size, function(p){ state.page=p; loadAdmissions(); });
      }).catch(function(err){ showToast(err.message, 'error'); empty(tblBody, 'Failed to load admissions'); });
    }
    _reload = loadAdmissions;
    loadAdmissions();
  }

})();
