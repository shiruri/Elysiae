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
    var role = Auth.getRole();

    /* ── Stat cards ── */
    function setStat(index, val, sub) {
      var cards = document.querySelectorAll('.s-value');
      if(cards.length > index) cards[index].textContent = val != null ? val : '--';
      var subs = document.querySelectorAll('.s-sub');
      if(subs.length > index && sub) subs[index].textContent = sub;
    }

    /* ADMIN gets full dashboard report */
    if(role === 'ADMIN') {
      API.get('/api/reports/dashboard').then(function(d) {
        setStat(0, d.totalPatients, '+0%');
        setStat(1, d.appointmentsToday, '+0%');
        setStat(2, d.occupiedBeds + '/' + d.totalBeds, d.availableBeds + ' available');
        setStat(3, '$' + (d.revenueToday || '0.00'), '+0%');
      }).catch(function(){});
    } else if(role === 'DOCTOR' || role === 'NURSE' || role === 'RECEPTIONIST') {
      API.post('/api/appointments?page=0&size=100', {}).then(function(d) {
        var items = d.content || d || [];
        var today = new Date().toISOString().slice(0,10);
        var todayAppts = items.filter(function(a){ return a.appointmentDateTime && a.appointmentDateTime.slice(0,10) === today; });
        setStat(0, items.length, 'Total appointments');
        setStat(1, todayAppts.length, 'Today');
        setStat(2, '--', '');
        setStat(3, '--', '');
      }).catch(function(){ setStat(0, '--'); setStat(1, '--'); });
    } else if(role === 'PATIENT') {
      API.get('/api/appointments/me?page=0&size=100').then(function(d) {
        var items = d.content || d || [];
        var today = new Date().toISOString().slice(0,10);
        var todayAppts = items.filter(function(a){ return a.appointmentDateTime && a.appointmentDateTime.slice(0,10) === today; });
        setStat(0, items.length, 'Total appointments');
        setStat(1, todayAppts.length, 'Today');
        setStat(2, '--', '');
        setStat(3, '--', '');
      }).catch(function(){});
    } else {
      setStat(0, '--'); setStat(1, '--'); setStat(2, '--'); setStat(3, '--');
    }

    /* ── Appointments table & Recent Activity ── */
    var tblBody = document.querySelector('.tbl table tbody');
    var activityCtn = document.querySelector('.activity');
    if(tblBody) {
      loading(tblBody);
      var apptPromise;
      if(role === 'ADMIN' || role === 'DOCTOR' || role === 'NURSE' || role === 'RECEPTIONIST') {
        apptPromise = API.post('/api/appointments?page=0&size=5', {});
      } else {
        apptPromise = API.get('/api/appointments/me?page=0&size=5');
      }
      apptPromise.then(function(d) {
        var items = d.content || d || [];
        if(!items.length) {
          empty(tblBody, 'No upcoming appointments', '<div class="empty-action"><button class="btn btn-primary btn-sm" onclick="openModal(\'modal-appt-create\')">Schedule first</button></div>');
          return;
        }
        tblBody.innerHTML = items.map(function(a){
          var statusMap = {SCHEDULED:'badge-teal',CONFIRMED:'badge-teal',IN_PROGRESS:'badge-amber',COMPLETED:'badge-green',CANCELLED:'badge-red',NO_SHOW:'badge-red'};
          var badgeClass = statusMap[a.status] || 'badge-teal';
          return '<tr><td>' + esc(a.patientFullName) + '</td><td>' + esc(a.doctorFullName) + '</td><td>' + esc(a.appointmentDateTime) + '</td><td><span class="badge ' + badgeClass + '">' + esc(a.status) + '</span></td><td class="text-right"><button class="btn btn-ghost btn-sm" onclick="openModal(\'modal-appt-update\')">Edit</button></td></tr>';
        }).join('');

        if(activityCtn) {
          activityCtn.innerHTML = items.slice(0,4).map(function(a){
            var dotColor = a.status === 'CANCELLED' ? 'red' : a.status === 'COMPLETED' ? 'green' : 'blue';
            var label = a.status === 'CANCELLED' ? 'Appointment cancelled' : 'Appointment ' + (a.status || 'booked').toLowerCase();
            return '<div class="activity-item"><div class="activity-dot ' + dotColor + '"></div><div><div class="activity-text">' + esc(label) + ' - ' + esc(a.patientFullName) + ' with Dr. ' + esc(a.doctorFullName) + '</div><div class="activity-time">' + esc(a.appointmentDateTime || '') + '</div></div></div>';
          }).join('');
        }
      }).catch(function(err){ showToast(err.message, 'error'); empty(tblBody, 'Failed to load appointments'); });

      if(role === 'ADMIN' && activityCtn) {
        API.get('/api/audit-log?page=0&size=3').then(function(d) {
          var logItems = d.content || [];
          if(!logItems.length) return;
          var actionColors = {PATIENT_CREATED:'green',PATIENT_UPDATED:'blue',APPOINTMENT_BOOKED:'teal',APPOINTMENT_CANCELLED:'red',PAYMENT_RECEIVED:'purple',INVOICE_GENERATED:'amber',PATIENT_ADMITTED:'green',PATIENT_DISCHARGED:'orange',USER_LOGIN:'blue',USER_LOGOUT:'gray'};
          var logHtml = logItems.map(function(a){
            var c = actionColors[a.action] || 'blue';
            var l = (a.action||'').replace(/_/g,' ').replace(/\b\w/g,function(c){return c.toUpperCase();});
            return '<div class="activity-item"><div class="activity-dot ' + c + '"></div><div><div class="activity-text">' + esc(l) + '</div><div class="activity-time">User #' + a.userid + '</div></div></div>';
          }).join('');
          activityCtn.innerHTML = logHtml + activityCtn.innerHTML;
        }).catch(function(){});
      }
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
      var queryStr = '?page=' + state.page + '&size=' + state.size;
      API.post('/api/appointments' + queryStr, {}).then(function(d) {
        var items = d.content || d || [];
        state.total = d.totalElements || items.length;
        var statusMap = {SCHEDULED:'badge-teal',CONFIRMED:'badge-teal',IN_PROGRESS:'badge-amber',COMPLETED:'badge-green',CANCELLED:'badge-red',NO_SHOW:'badge-red'};
        if(!items.length) {
          empty(tblBody, 'No appointments found', '<div class="empty-action"><button class="btn btn-primary btn-sm" onclick="openModal(\'modal-appt-create\')">Create first</button></div>');
        } else {
          tblBody.innerHTML = items.map(function(a){
            var badgeClass = statusMap[a.status] || 'badge-teal';
            return '<tr><td>' + esc(a.patientFullName) + '</td><td>' + esc(a.doctorFullName) + '</td><td>' + esc(a.appointmentDateTime) + '</td><td><span class="badge ' + badgeClass + '">' + esc(a.status) + '</span></td><td class="text-right"><div class="flex gap-1 justify-end"><button class="btn btn-ghost btn-sm" onclick="openModal(\'modal-appt-update\')">Edit</button></div></td></tr>';
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

  /* ── Staff (Users) ── */
  if(page === 'staff') {
    var tblBody = document.querySelector('.tbl table tbody');
    var searchInput = document.querySelector('.filter-bar input');
    var filterBtn = document.querySelector('.filter-bar .btn-primary');
    var paginationCtn = document.querySelector('.tbl') ? (function(){ var d=document.createElement('div'); document.querySelector('.tbl').appendChild(d); return d; })() : null;

    function loadStaff() {
      if(!tblBody) return;
      loading(tblBody);
      var body = {page:state.page, size:state.size};
      if(state.query) body.keyword = state.query;
      API.post('/api/auth/search-users', body).then(function(d) {
        var items = d.content || d || [];
        state.total = d.totalElements || items.length;
        if(!items.length) {
          empty(tblBody, 'No staff accounts found', '<div class="empty-action"><button class="btn btn-primary btn-sm" onclick="openModal(\'modal-user-create\')">Create staff account</button></div>');
        } else {
          var roleLabels = {ADMIN:'Admin',DOCTOR:'Doctor',NURSE:'Nurse',RECEPTIONIST:'Receptionist',LAB_TECH:'Lab Tech',PHARMACIST:'Pharmacist',CASHIER:'Cashier',PATIENT:'Patient'};
          tblBody.innerHTML = items.map(function(u){
            var roleLabel = roleLabels[u.role] || u.role;
            var statusHtml = u.mustChangePassword ? '<span class="badge badge-amber">Temp</span>' : '<span class="badge badge-green">Active</span>';
            return '<tr><td><span class="font-medium">' + esc(u.username) + '</span></td><td>' + roleLabel + '</td><td>' + statusHtml + '</td><td class="text-right"><div class="flex gap-1 justify-end"><button class="btn btn-ghost btn-sm" onclick="openModal(\'modal-user-update\')">Edit</button><button class="btn btn-ghost btn-sm text-red-500" onclick="confirm(\'Delete user?\')&&API.del(\'/api/auth/'+u.id+'\').then(function(){loadStaff();showToast(\'Deleted\',\'success\');}).catch(function(e){showToast(e.message,\'error\');})">Del</button></div></td></tr>';
          }).join('');
        }
        pagination(paginationCtn, state.total, state.page, state.size, function(p){ state.page=p; loadStaff(); });
      }).catch(function(err){ showToast(err.message, 'error'); empty(tblBody, 'Failed to load staff'); });
    }

    if(filterBtn) {
      filterBtn.addEventListener('click', function(){
        state.query = searchInput ? searchInput.value : '';
        state.page = 0;
        loadStaff();
      });
    }
    if(searchInput) {
      searchInput.addEventListener('keydown', function(e){ if(e.key==='Enter'&&filterBtn)filterBtn.click(); });
    }

    _reload = loadStaff;
    loadStaff();
  }

  /* ── Reports ── */
  if(page === 'reports') {
    API.get('/api/reports/dashboard').then(function(d) {
      var cards = document.querySelectorAll('.s-value');
      if(cards.length >= 4) {
        cards[0].textContent = d.appointmentsToday != null ? d.appointmentsToday : '--';
        cards[1].textContent = d.totalRevenue != null ? '$' + d.totalRevenue : '$--';
        cards[2].textContent = d.occupiedBeds != null && d.totalBeds ? Math.round(d.occupiedBeds/d.totalBeds*100) + '%' : '--%';
        cards[3].textContent = d.occupiedBeds != null ? d.occupiedBeds : '--';
      }
      var subs = document.querySelectorAll('.s-sub');
      if(subs.length >= 4 && d.availableBeds != null) {
        subs[0].textContent = d.availableBeds + ' available';
        subs[1].textContent = d.totalRevenue ? 'Total revenue' : '--';
        subs[2].textContent = d.totalBeds ? d.totalBeds + ' total beds' : '--';
        subs[3].textContent = d.occupiedBeds + ' occupied';
      }
    }).catch(function(err){ showToast(err.message, 'error'); });

    var tblBody = document.querySelector('.tbl table tbody');
    if(tblBody) {
      loading(tblBody);
      API.get('/api/audit-log').then(function(d) {
        var items = d.content || d || [];
        if(!items.length) {
          empty(tblBody, 'No activity logged yet');
        } else {
          tblBody.innerHTML = items.map(function(a){
            return '<tr><td>' + esc(a.userid || '--') + '</td><td>' + esc(a.action || '--') + '</td><td>--</td><td>--</td><td class="text-right"><button class="btn btn-ghost btn-sm">View</button></td></tr>';
          }).join('');
        }
      }).catch(function(err){ empty(tblBody, 'No activity logged yet'); });
    }
  }

  /* ── Service Rate ── */
  if(page === 'service-rate') {
    var tblBody = document.querySelector('.tbl table tbody');
    var filterBtn = document.querySelector('.filter-bar .btn-primary');
    var searchInput = document.querySelector('.filter-bar input');

    function renderServiceRates(items) {
      window._serviceRates = items;
      var totalEl = document.getElementById('sr-total');
      var activeEl = document.getElementById('sr-active');
      var inactiveEl = document.getElementById('sr-inactive');
      var typesEl = document.getElementById('sr-types');
      var activeCount = items.filter(function(r){ return r.isActive === true || r.active === true; }).length;
      if(totalEl) totalEl.textContent = items.length;
      if(activeEl) activeEl.textContent = activeCount;
      if(inactiveEl) inactiveEl.textContent = items.length - activeCount;
      if(typesEl) {
        var unique = items.map(function(r){ return r.type; }).filter(function(v,i,a){ return a.indexOf(v)===i; });
        typesEl.textContent = unique.length;
      }
      if(!items.length) {
        empty(tblBody, 'No service rates configured');
      } else {
        tblBody.innerHTML = items.map(function(r){
          var isActive = r.isActive === true || r.active === true;
          var activeBadge = isActive ? '<span class="badge badge-green">Active</span>' : '<span class="badge badge-red">Inactive</span>';
          return '<tr><td><span class="font-medium">' + esc(r.type || r.serviceKey) + '</span></td><td>' + esc(r.serviceKey || '') + '</td><td><span class="font-medium">$' + (r.rate || '0.00') + '</span></td><td>' + activeBadge + '</td><td class="text-right"><button class="btn btn-ghost btn-sm" onclick="editServiceRate(' + r.serviceRate + ')">Edit</button></td></tr>';
        }).join('');
      }
    }

    function loadServiceRates() {
      if(!tblBody) return;
      loading(tblBody);
      window._allRates = [];
      API.get('/api/billing/service-rate').then(function(data) {
        var items = data || [];
        window._allRates = items;
        renderServiceRates(items);
      }).catch(function(err){ showToast(err.message, 'error'); empty(tblBody, 'Failed to load service rates'); });
    }

    if(filterBtn && searchInput) {
      filterBtn.addEventListener('click', function(){
        var q = searchInput.value.toLowerCase();
        var filtered = window._allRates.filter(function(r){
          return (r.type || '').toLowerCase().indexOf(q) !== -1 || (r.serviceKey || '').toLowerCase().indexOf(q) !== -1;
        });
        renderServiceRates(filtered);
      });
      searchInput.addEventListener('keydown', function(e){ if(e.key==='Enter') filterBtn.click(); });
    }

    window.editServiceRate = function(id) {
      var items = window._serviceRates || [];
      var rate = items.find(function(r){ return r.serviceRate === id; });
      if(!rate) return;
      var el = document.getElementById('modal-service-rate-update');
      if(!el) return;
      var idInput = el.querySelector('#sr-id');
      var typeInput = el.querySelector('#sr-type');
      var rateInput = el.querySelector('#sr-rate');
      var descInput = el.querySelector('#sr-description');
      var activeInput = el.querySelector('#sr-isActive');
      if(idInput) idInput.value = rate.serviceRate;
      if(typeInput) typeInput.value = rate.type || '';
      if(rateInput) rateInput.value = rate.rate;
      if(descInput) descInput.value = rate.description || '';
      if(activeInput) activeInput.value = rate.isActive === true || rate.active === true ? 'true' : 'false';
      openModal('modal-service-rate-update');
    };

    _reload = loadServiceRates;
    loadServiceRates();
  }

  /* ── Audit ── */
  if(page === 'audit') {
    var tblBody = document.querySelector('.tbl table tbody');
    var filterBtn = document.querySelector('.filter-bar .btn-primary');
    var searchInput = document.querySelector('.filter-bar input');
    var paginationCtn = document.querySelector('.tbl') ? (function(){ var d=document.createElement('div'); document.querySelector('.tbl').appendChild(d); return d; })() : null;

    function loadAudit() {
      if(!tblBody) return;
      loading(tblBody);
      var q = state.query ? '?action=' + encodeURIComponent(state.query) : '';
      API.get('/api/audit-log' + q).then(function(d) {
        var items = d.content || d || [];
        state.total = d.totalElements || items.length;
        if(!items.length) {
          empty(tblBody, 'No audit logs found');
        } else {
          tblBody.innerHTML = items.map(function(a){
            return '<tr><td>' + esc(a.userid || '--') + '</td><td><span class="badge badge-teal">' + esc(a.action || '--') + '</span></td><td>--</td><td>--</td></tr>';
          }).join('');
        }
        pagination(paginationCtn, state.total, state.page, state.size, function(p){ state.page=p; loadAudit(); });
      }).catch(function(err){ showToast(err.message, 'error'); empty(tblBody, 'Failed to load audit logs'); });
    }

    if(filterBtn) {
      filterBtn.addEventListener('click', function(){
        state.query = searchInput ? searchInput.value : '';
        state.page = 0;
        loadAudit();
      });
    }
    if(searchInput) {
      searchInput.addEventListener('keydown', function(e){ if(e.key==='Enter'&&filterBtn)filterBtn.click(); });
    }

    _reload = loadAudit;
    loadAudit();
  }

})();
