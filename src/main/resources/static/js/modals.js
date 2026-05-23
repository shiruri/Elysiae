/* Elysiae Modal System — all modals with DTO-matched fields */
(function(){
  'use strict';

  /* ---------- helpers ---------- */
  function esc(s){ return (s||'').replace(/&/g,'&amp;').replace(/"/g,'&quot;').replace(/</g,'&lt;'); }

  function input(id, label, type, attrs){
    var a = attrs || '';
    return '<div class="form-group">'
      +'<label class="form-label" for="'+id+'">'+label+'</label>'
      +'<input type="'+type+'" id="'+id+'" '+a+' class="form-input">'
      +'</div>';
  }
  function select(id, label, options, attrs){
    var a = attrs || '';
    var opts = '';
    options.forEach(function(o){
      if(typeof o === 'string') opts += '<option value="'+o+'">'+o+'</option>';
      else opts += '<option value="'+(o.v||'')+'">'+(o.l||o.v||'')+'</option>';
    });
    return '<div class="form-group">'
      +'<label class="form-label" for="'+id+'">'+label+'</label>'
      +'<select id="'+id+'" '+a+' class="form-input">'+opts+'</select>'
      +'</div>';
  }
  function textarea(id, label, attrs){
    var a = attrs || '';
    return '<div class="form-group">'
      +'<label class="form-label" for="'+id+'">'+label+'</label>'
      +'<textarea id="'+id+'" '+a+' class="form-input" rows="3"></textarea>'
      +'</div>';
  }
  function fieldset(legend, content){
    return '<fieldset class="modal-fieldset"><legend class="modal-legend">'+legend+'</legend>'+content+'</fieldset>';
  }
  function row(a, b){
    return '<div class="form-row">'+a+b+'</div>';
  }
  function pickerInput(id, label, entityType, attrs){
    var a = attrs || '';
    return '<div class="form-group">'
      +'<label class="form-label" for="'+id+'">'+label+'</label>'
      +'<div class="input-with-picker">'
        +'<input type="number" id="'+id+'" '+a+' class="form-input" placeholder="">'
        +'<button class="btn-picker" onclick="openPicker(\''+id+'\',\''+entityType+'\')" type="button" aria-label="Search">'
          +'<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>'
        +'</button>'
      +'</div>'
    +'</div>';
  }

  function modalHTML(id, title, body, footer){
    return '<div class="modal-overlay" id="'+id+'">'
      +'<div class="modal" role="dialog" aria-modal="true">'
        +'<div class="modal-hdr">'
          +'<h2>'+title+'</h2>'
          +'<button class="modal-close" onclick="closeModal(\''+id+'\')" aria-label="Close">'
            +'<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>'
          +'</button>'
        +'</div>'
        +'<div class="modal-body">'+body+'</div>'
        +'<div class="modal-ftr">'+(footer||'')+'</div>'
      +'</div>'
    +'</div>';
  }

  var FTR = '<button class="btn btn-outline btn-sm" onclick="closeModal(this.closest(\'.modal-overlay\').id)">Cancel</button>'
          + '<button class="btn btn-primary btn-sm" onclick="handleModalSubmit(this.closest(\'.modal-overlay\').id)">Submit</button>';

  /* ---------- picker data (fetched from API) ---------- */
  var PIC_DATA = {};
  var _pickerField = null;
  var _pickerType = null;
  var _pickerLoading = false;

  var PICKER_SOURCES = {
    patient: { search: 'POST', path: '/api/patients/get-patients', body: {page:0,size:50},
      map: function(r){ return (r.content||r).map(function(i){ return {id:i.id,name:(i.firstName||'')+' '+(i.lastName||''),detail:'DOB: '+(i.dateOfBirth||'')}; }); }},
    doctor: { search: 'POST', path: '/api/doctor', body: {page:0,size:50},
      map: function(r){ return (r.content||r).map(function(i){ return {id:i.id,name:'Dr. '+(i.firstName||'')+' '+(i.lastName||''),detail:(i.specialization||'')+' | '+(i.departmentName||'')}; }); }},
    department: { search: 'GET', path: '/api/department', body: null,
      map: function(r){ return (r.content||r).map(function(i){ return {id:i.id,name:i.name||'',detail:'Floor: '+(i.floor||'')}; }); }},
    ward: { search: 'POST', path: '/api/wards/search', body: {page:0,size:50},
      map: function(r){ return (r.content||r).map(function(i){ return {id:i.id,name:i.name||'',detail:(i.type||'')+' - Floor '+(i.floor||'')}; }); }},
    bed: { search: 'GET', path: '/api/wards/beds/0', body: null,
      map: function(r){ return (r.content||r).map(function(i){ return {id:i.id,name:'Bed '+(i.bedNumber||''),detail:(i.wardName||'')+' ['+(i.status||'')+']'}; }); }},
    appointment: { search: 'POST', path: '/api/appointments', body: {page:0,size:50},
      map: function(r){ return (r.content||r).map(function(i){ return {id:i.id,name:'Appt #'+i.id,detail:(i.patientName||'')+' @ '+(i.appointmentDateTime||'')}; }); }},
    lab: { search: 'POST', path: '/api/lab/request/search', body: {page:0,size:50},
      map: function(r){ return (r.content||r).map(function(i){ return {id:i.id,name:'Lab #'+i.id,detail:(i.testType||'')+' - '+(i.patientName||'')}; }); }},
    record: { search: 'GET', path: '/api/records', body: null,
      map: function(r){ return (r.content||[]).map(function(i){ return {id:i.id,name:'Record #'+i.id,detail:(i.diagnosis||'')+' - '+(i.patientName||'')}; }); }},
    admission: { search: 'POST', path: '/api/admission/search', body: {page:0,size:50},
      map: function(r){ return (r.content||[]).map(function(i){ return {id:i.id,name:'Adm #'+i.id,detail:(i.patientName||'')+' ['+(i.status||'')+']'}; }); }}
  };

  function pickerHTML(){
    return '<div class="picker-overlay" id="picker-overlay" onclick="if(event.target===this)closePicker()">'
      +'<div class="picker-panel">'
        +'<div class="picker-hdr">'
          +'<h2>Select <span id="picker-title">Patient</span></h2>'
          +'<button class="picker-close" onclick="closePicker()">'
            +'<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>'
          +'</button>'
        +'</div>'
        +'<div class="picker-search">'
          +'<svg class="picker-search-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>'
          +'<input type="text" id="picker-search-input" class="picker-search-input" placeholder="Search..." oninput="filterPicker(this.value)">'
        +'</div>'
        +'<div class="picker-list" id="picker-list"></div>'
      +'</div>'
    +'</div>';
  }

  function renderPickerItems(type, query){
    var items = PIC_DATA[type] || [];
    if(query){
      var q = query.toLowerCase();
      items = items.filter(function(i){ return i.name.toLowerCase().indexOf(q) !== -1 || i.detail.toLowerCase().indexOf(q) !== -1; });
    }
    var list = document.getElementById('picker-list');
    if(!list) return;
    if(!items.length){
      list.innerHTML = '<div class="picker-empty"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/><line x1="8" y1="11" x2="14" y2="11"/></svg><p>No results found</p></div>';
      return;
    }
    list.innerHTML = items.map(function(item){
      return '<div class="picker-item" onclick="handlePickerSelect('+item.id+')">'
        +'<div class="picker-item-id">#'+item.id+'</div>'
        +'<div class="picker-item-info">'
          +'<div class="picker-item-name">'+esc(item.name)+'</div>'
          +'<div class="picker-item-detail">'+esc(item.detail)+'</div>'
        +'</div>'
      +'</div>';
    }).join('');
  }

  /* ---------- modal definitions ---------- */
  var modals = [

    /* PATIENT — Create (PatientCreateRequest) */
    ['modal-patient-create', 'Register Patient',
      fieldset('Personal Information',
        input('p-username','Username','text','required minlength="3" maxlength="80" placeholder="patient_john"')
        + row(input('p-firstName','First Name','text','required maxlength="100" placeholder="John"'),
              input('p-lastName','Last Name','text','required maxlength="100" placeholder="Doe"'))
        + input('p-dateOfBirth','Date of Birth','date','required'))
      + fieldset('Medical Info',
        row(select('p-gender','Gender',[{v:'',l:'Select...'},{v:'MALE',l:'Male'},{v:'FEMALE',l:'Female'},{v:'OTHER',l:'Other'}]),
            select('p-bloodType','Blood Type',[{v:'',l:'Select...'},{v:'A+',l:'A+'},{v:'A-',l:'A-'},{v:'B+',l:'B+'},{v:'B-',l:'B-'},{v:'AB+',l:'AB+'},{v:'AB-',l:'AB-'},{v:'O+',l:'O+'},{v:'O-',l:'O-'}])))
      + fieldset('Contact',
        input('p-phone','Phone','tel','pattern="\\+?[0-9]{10,15}" placeholder="+639123456789"')
        + input('p-email','Email','email','maxlength="120" placeholder="john@email.com"')
        + input('p-address','Address','text','placeholder="123 Main St"'))
      + fieldset('Emergency Contact',
        row(input('p-ecName','Emergency Contact Name','text','placeholder="Jane Doe"'),
            input('p-ecPhone','Emergency Contact Phone','tel','pattern="\\+?[0-9]{10,15}" placeholder="+639987654321"'))),
      FTR],

    /* PATIENT — Update (PatientUpdateRequest) */
    ['modal-patient-update', 'Update Patient',
      fieldset('Personal Information',
        row(input('pu-firstName','First Name','text','maxlength="100" placeholder="John"'),
            input('pu-lastName','Last Name','text','maxlength="100" placeholder="Doe"'))
        + input('pu-dateOfBirth','Date of Birth','date',''))
      + fieldset('Medical Info',
        row(select('pu-gender','Gender',[{v:'',l:'Select...'},{v:'MALE',l:'Male'},{v:'FEMALE',l:'Female'},{v:'OTHER',l:'Other'}]),
            select('pu-bloodType','Blood Type',[{v:'',l:'Select...'},{v:'A+',l:'A+'},{v:'A-',l:'A-'},{v:'B+',l:'B+'},{v:'B-',l:'B-'},{v:'AB+',l:'AB+'},{v:'AB-',l:'AB-'},{v:'O+',l:'O+'},{v:'O-',l:'O-'}])))
      + fieldset('Contact',
        input('pu-phone','Phone','tel','pattern="\\+?[0-9]{10,15}" placeholder="+639123456789"')
        + input('pu-email','Email','email','maxlength="120" placeholder="john@email.com"')
        + input('pu-address','Address','text','placeholder="123 Main St"'))
      + fieldset('Emergency Contact',
        row(input('pu-ecName','Emergency Contact Name','text','placeholder="Jane Doe"'),
            input('pu-ecPhone','Emergency Contact Phone','tel','pattern="\\+?[0-9]{10,15}" placeholder="+639987654321"'))),
      FTR],

    /* DOCTOR — Create (DoctorCreateRequest) */
    ['modal-doctor-create', 'Register Doctor',
      fieldset('Account',
        input('d-username','Username','text','required minlength="3" maxlength="80" placeholder="dr_smith"'))
      + fieldset('Professional Info',
        pickerInput('d-departmentId','Department ID','department','required min="1" placeholder="1"')
        + row(input('d-firstName','First Name','text','required maxlength="100" placeholder="John"'),
              input('d-lastName','Last Name','text','required maxlength="100" placeholder="Smith"'))
        + input('d-specialization','Specialization','text','maxlength="150" placeholder="Cardiology"')
        + input('d-licenseNumber','License Number','text','maxlength="80" placeholder="LIC-001"'))
      + fieldset('Contact',
        input('d-phone','Phone','tel','maxlength="20" placeholder="+639123456789"')),
      FTR],

    /* DOCTOR — Update (DoctorUpdateRequest) */
    ['modal-doctor-update', 'Update Doctor',
      fieldset('Professional Info',
        pickerInput('du-departmentId','Department ID','department','min="1" placeholder="1"')
        + row(input('du-firstName','First Name','text','maxlength="100" placeholder="John"'),
              input('du-lastName','Last Name','text','maxlength="100" placeholder="Smith"'))
        + input('du-specialization','Specialization','text','maxlength="150" placeholder="Cardiology"')
        + input('du-licenseNumber','License Number','text','maxlength="80" placeholder="LIC-001"'))
      + fieldset('Contact',
        input('du-phone','Phone','tel','maxlength="20" placeholder="+639123456789"')),
      FTR],

    /* DOCTOR — Schedule Update (DoctorScheduleUpdateRequest) */
    ['modal-doctor-schedule', 'Update Doctor Schedule',
      fieldset('Time Slot',
        select('ds-dayOfWeek','Day of Week',[{v:'MON',l:'Monday'},{v:'TUE',l:'Tuesday'},{v:'WED',l:'Wednesday'},{v:'THU',l:'Thursday'},{v:'FRI',l:'Friday'},{v:'SAT',l:'Saturday'},{v:'SUN',l:'Sunday'}],'required')
        + row(input('ds-startTime','Start Time','time','required'),
              input('ds-endTime','End Time','time','required'))
        + input('ds-slotDurationMinutes','Slot Duration (minutes)','number','min="5" max="120" placeholder="30"')),
      FTR],

    /* DEPARTMENT — Create (DepartmentCreateRequest) */
    ['modal-dept-create', 'Create Department',
      fieldset('Department Info',
        input('dept-name','Department Name','text','required maxlength="100" placeholder="Cardiology"')
        + input('dept-floor','Floor','text','maxlength="20" placeholder="2nd Floor"')),
      FTR],

    /* DEPARTMENT — Update (DepartmentUpdateRequest) */
    ['modal-dept-update', 'Update Department',
      fieldset('Department Info',
        input('deptu-id','Department ID','number','required min="1" placeholder="1"')
        + input('deptu-name','Department Name','text','maxlength="100" placeholder="Cardiology Updated"')
        + input('deptu-floor','Floor','text','maxlength="20" placeholder="3rd Floor"')),
      FTR],

    /* WARD — Create (WardCreateRequest) */
    ['modal-ward-create', 'Create Ward',
      fieldset('Ward Info',
        input('w-name','Ward Name','text','required maxlength="100" placeholder="General Ward"')
        + select('w-type','Ward Type',[{v:'GENERAL',l:'General'},{v:'ICU',l:'ICU'},{v:'PRIVATE',l:'Private'},{v:'PEDIATRIC',l:'Pediatric'},{v:'MATERNITY',l:'Maternity'},{v:'ISOLATION',l:'Isolation'},{v:'EMERGENCY',l:'Emergency'}],'required')
        + input('w-floor','Floor','text','maxlength="20" placeholder="1st"')),
      FTR],

    /* BED — Add (BedAddRequest) */
    ['modal-bed-create', 'Add Bed',
      fieldset('Bed Info',
        pickerInput('b-wardId','Ward ID','ward','required min="1" placeholder="1"')
        + input('b-bedNo','Bed Number','text','required minlength="1" maxlength="20" placeholder="A1"')
        + select('b-status','Status',[{v:'AVAILABLE',l:'Available'},{v:'OCCUPIED',l:'Occupied'},{v:'MAINTENANCE',l:'Maintenance'},{v:'RESERVED',l:'Reserved'}],'required')),
      FTR],

    /* ADMISSION — Admit (BedAdmitPatientRequest) */
    ['modal-admission-create', 'Admit Patient',
      fieldset('Admission Details',
        pickerInput('adm-patientId','Patient ID','patient','required min="1" placeholder="1"')
        + pickerInput('adm-bedId','Bed ID','bed','required min="1" placeholder="1"')
        + pickerInput('adm-doctorId','Doctor ID','doctor','required min="1" placeholder="1"')),
      FTR],

    /* ADMISSION — Transfer (AdmissionTransferRequest) */
    ['modal-admission-transfer', 'Transfer Patient',
      fieldset('Transfer Details',
        pickerInput('atf-patientId','Patient ID','patient','required min="1" placeholder="1"')
        + pickerInput('atf-newBedId','New Bed ID','bed','required min="1" placeholder="2"')
        + pickerInput('atf-newDoctorId','New Doctor ID (optional)','doctor','min="1" placeholder="2"')),
      FTR],

    /* APPOINTMENT — Create (AppointmentCreateRequest) */
    ['modal-appt-create', 'Create Appointment',
      fieldset('Appointment Details',
        row(pickerInput('ap-patientId','Patient ID','patient','required min="1" placeholder="1"'),
            pickerInput('ap-doctorId','Doctor ID','doctor','required min="1" placeholder="1"'))
        + input('ap-appointmentDateTime','Date & Time','datetime-local','required')
        + select('ap-type','Type',[{v:'CONSULTATION',l:'Consultation'},{v:'FOLLOW_UP',l:'Follow Up'},{v:'EMERGENCY',l:'Emergency'},{v:'CHECKUP',l:'Checkup'},{v:'PROCEDURE',l:'Procedure'}])),
      FTR],

    /* APPOINTMENT — Update (AppointmentUpdateRequest) */
    ['modal-appt-update', 'Update Appointment',
      fieldset('Appointment Details',
        select('apu-type','Type',[{v:'',l:'Select...'},{v:'CONSULTATION',l:'Consultation'},{v:'FOLLOW_UP',l:'Follow Up'},{v:'EMERGENCY',l:'Emergency'},{v:'CHECKUP',l:'Checkup'},{v:'PROCEDURE',l:'Procedure'}])
        + input('apu-appointmentDate','New Date & Time','datetime-local','')
        + textarea('apu-notes','Notes','placeholder="Follow-up visit notes..."')),
      FTR],

    /* LAB — Create Request (LabRequestCreateRequest) */
    ['modal-lab-create', 'Create Lab Request',
      fieldset('Request Details',
        row(pickerInput('lab-patientId','Patient ID','patient','required min="1" placeholder="1"'),
            pickerInput('lab-doctorId','Doctor ID','doctor','required min="1" placeholder="1"'))
        + input('lab-testType','Test Type','text','required maxlength="150" placeholder="BLOOD_TEST"')
        + select('lab-priority','Priority',[{v:'',l:'Routine (default)'},{v:'ROUTINE',l:'Routine'},{v:'URGENT',l:'Urgent'},{v:'STAT',l:'STAT'}])),
      FTR],

    /* LAB — Post Result (LabResultCreateRequest) */
    ['modal-lab-result', 'Post Lab Result',
      fieldset('Result Details',
        pickerInput('lr-labRequestId','Lab Request ID','lab','required min="1" placeholder="1"')
        + input('lr-resultValue','Result Value','text','required placeholder="Normal"')
        + input('lr-normalRange','Normal Range','text','placeholder="0-100"')
        + row(select('lr-isAbnormal','Is Abnormal',[{v:'false',l:'No'},{v:'true',l:'Yes'}]),
              input('lr-remarks','Remarks','text','placeholder="All clear"'))),
      FTR],

    /* VITALS — Create (VitalsCreateRequest) */
    ['modal-vitals-create', 'Log Vitals',
      fieldset('Patient',
        pickerInput('v-patientId','Patient ID','patient','required min="1" placeholder="1"'))
      + fieldset('Vitals',
        row(input('v-temperature','Temperature (°C)','number','step="0.1" placeholder="37.2"'),
            input('v-bloodPressure','Blood Pressure','text','placeholder="120/80"'))
        + row(input('v-heartRate','Heart Rate (bpm)','number','min="0" placeholder="72"'),
              input('v-oxygenSat','Oxygen Saturation (%)','number','step="0.1" placeholder="98.5"'))
        + row(input('v-weightKg','Weight (kg)','number','step="0.1" placeholder="70.5"'),
              input('v-heightCm','Height (cm)','number','step="0.1" placeholder="175.0"'))),
      FTR],

    /* MEDICAL RECORD — Create (MedicalRecordCreateRequest) */
    ['modal-record-create', 'Create Medical Record',
      fieldset('Record Info',
        row(pickerInput('mr-patientId','Patient ID','patient','required min="1" placeholder="1"'),
            pickerInput('mr-doctorId','Doctor ID','doctor','required min="1" placeholder="1"'))
        + row(pickerInput('mr-appointmentId','Appointment ID (optional)','appointment','min="1" placeholder="1"'),
              pickerInput('mr-admissionId','Admission ID (optional)','admission','min="1" placeholder="1"')))
      + fieldset('Clinical',
        textarea('mr-diagnosis','Diagnosis','placeholder="Influenza A"')
        + textarea('mr-notes','Notes','placeholder="Rest and fluids recommended"')),
      FTR],

    /* MEDICAL RECORD — Update (MedicalRecordUpdateRequest) */
    ['modal-record-update', 'Update Medical Record',
      fieldset('Record',
        pickerInput('mru-recordId','Record ID','record','required min="1" placeholder="1"'))
      + fieldset('Clinical',
        textarea('mru-diagnosis','Diagnosis','placeholder="Updated diagnosis"')
        + textarea('mru-notes','Notes','placeholder="Updated notes"')),
      FTR],

    /* PRESCRIPTION — Create (PrescriptionCreateRequest) */
    ['modal-prescription-create', 'Add Prescription',
      fieldset('Prescription',
        pickerInput('rx-recordId','Medical Record ID','record','required min="1" placeholder="1"')
        + input('rx-medicineName','Medicine Name','text','required maxlength="200" placeholder="Paracetamol 500mg"')
        + row(input('rx-dosage','Dosage','text','maxlength="100" placeholder="500mg"'),
              input('rx-frequency','Frequency','text','maxlength="100" placeholder="3x/day"'))
        + input('rx-durationDays','Duration (days)','number','min="1" placeholder="7"')),
      FTR],

    /* USER — Create (UserCreateRequest) */
    ['modal-user-create', 'Create User',
      fieldset('Account',
        input('u-username','Username','text','required minlength="6" maxlength="100" placeholder="newuser"')
        + select('u-role','Role',[{v:'ADMIN',l:'Admin'},{v:'DOCTOR',l:'Doctor'},{v:'NURSE',l:'Nurse'},{v:'PATIENT',l:'Patient'},{v:'RECEPTIONIST',l:'Receptionist'},{v:'PHARMACIST',l:'Pharmacist'},{v:'LAB_TECH',l:'Lab Tech'},{v:'CASHIER',l:'Cashier'}],'required')),
      FTR],

    /* USER — Update (UserUpdateRequest) */
    ['modal-user-update', 'Update User',
      fieldset('Account',
        input('uu-username','Username','text','minlength="6" maxlength="100" placeholder="updateduser"')
        + select('uu-role','Role',[{v:'',l:'Select...'},{v:'ADMIN',l:'Admin'},{v:'DOCTOR',l:'Doctor'},{v:'NURSE',l:'Nurse'},{v:'PATIENT',l:'Patient'},{v:'RECEPTIONIST',l:'Receptionist'},{v:'PHARMACIST',l:'Pharmacist'},{v:'LAB_TECH',l:'Lab Tech'},{v:'CASHIER',l:'Cashier'}])),
      FTR],

    /* INVOICE — Create (InvoiceCreateRequest) */
    ['modal-invoice-create', 'Create Invoice',
      fieldset('Invoice Details',
        pickerInput('inv-patientId','Patient ID','patient','required min="1" placeholder="1"')
        + input('inv-admissionId','Admission ID (optional)','number','min="1" placeholder="1"')
        + input('inv-dueDate','Due Date','date','required')),
      FTR],

    /* USER — Change Password (UserChangePasswordRequest) */
    ['modal-user-password', 'Change Password',
      fieldset('Password',
        input('cp-oldPassword','Old Password','password','required maxlength="255"')
        + input('cp-newPassword','New Password','password','required maxlength="255"')),
      FTR],
  ];

  /* ---------- inject ---------- */
  function init(){
    var html = '';
    modals.forEach(function(m){
      html += modalHTML(m[0], m[1], m[2], m[3]);
    });
    (document.body || document.documentElement).insertAdjacentHTML('beforeend', html);
    (document.body || document.documentElement).insertAdjacentHTML('beforeend', pickerHTML());

    /* click outside to close */
    document.addEventListener('click', function(e){
      if(e.target.classList.contains('modal-overlay')){
        closeModal(e.target.id);
      }
    });

    /* Escape key to close */
    document.addEventListener('keydown', function(e){
      if(e.key === 'Escape'){
        var picker = document.getElementById('picker-overlay');
        if(picker && picker.classList.contains('open')){ closePicker(); return; }
        var active = document.querySelector('.modal-overlay.active');
        if(active) closeModal(active.id);
      }
    });
  }

  if(document.readyState === 'loading'){
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  /* ---------- modal-to-API mapping ---------- */
  var MODAL_API = {
    'modal-patient-create':     { m:'POST',  path:'/api/patients' },
    'modal-patient-update':     { m:'PATCH', path:'/api/patients/{id}' },
    'modal-doctor-create':      { m:'POST',  path:'/api/doctor/register' },
    'modal-doctor-update':      { m:'PATCH', path:'/api/doctor/update/{id}' },
    'modal-doctor-schedule':    { m:'PATCH', path:'/api/doctor/update/{id}/schedule' },
    'modal-dept-create':        { m:'POST',  path:'/api/department/register' },
    'modal-dept-update':        { m:'PATCH', path:'/api/department' },
    'modal-ward-create':        { m:'POST',  path:'/api/wards/register/ward' },
    'modal-bed-create':         { m:'POST',  path:'/api/wards/add/bed' },
    'modal-admission-create':   { m:'POST',  path:'/api/admission/ward/admissions' },
    'modal-admission-transfer': { m:'PATCH', path:'/api/admission/transfer' },
    'modal-appt-create':        { m:'POST',  path:'/api/appointments/create' },
    'modal-appt-update':        { m:'PATCH', path:'/api/appointments/{id}' },
    'modal-lab-create':         { m:'POST',  path:'/api/lab/request' },
    'modal-lab-result':         { m:'POST',  path:'/api/lab/result' },
    'modal-vitals-create':      { m:'POST',  path:'/api/vitals' },
    'modal-record-create':      { m:'POST',  path:'/api/records/create' },
    'modal-record-update':      { m:'PATCH', path:'/api/records' },
    'modal-prescription-create':{ m:'POST',  path:'/api/records/prescription' },
    'modal-invoice-create':     { m:'POST',  path:'/api/billing/generate/invoice' },
    'modal-user-create':        { m:'POST',  path:'/api/auth/register' },
    'modal-user-update':        { m:'PATCH', path:'/api/auth/update/{id}' },
    'modal-user-password':      { m:'PATCH', path:'/api/auth/change-password/{id}' }
  };

  window.openModal = function(id){
    var el = document.getElementById(id);
    if(el){ el.classList.add('active'); document.body.style.overflow='hidden'; }
  };
  window.closeModal = function(id){
    var el = typeof id === 'string' ? document.getElementById(id) : document.getElementById(id);
    if(el){ el.classList.remove('active'); document.body.style.overflow=''; }
    closePicker();
  };
  window.handleModalSubmit = function(id){
    var overlay = document.getElementById(id);
    if(!overlay) return;
    var apiDef = MODAL_API[id];
    if(!apiDef){
      showToast('No API endpoint configured for this form', 'error');
      return;
    }

    var fields = {};
    var inputs = overlay.querySelectorAll('input, select, textarea');
    inputs.forEach(function(inp){
      if(inp.id){
        var key = inp.id.replace(/^[a-z]+-/, '');
        var val = inp.value;
        if(inp.type === 'number' && val !== '') val = Number(val);
        else if(inp.type === 'datetime-local' && val !== '') val = val;
        else if(val === '') val = null;
        else if(val === 'true') val = true;
        else if(val === 'false') val = false;
        fields[key] = val;
      }
    });

    var requestId = fields.id || null;
    var path = apiDef.path;
    if(requestId && path.indexOf('{id}') !== -1){
      path = path.replace('{id}', requestId);
      delete fields.id;
    } else if(path.indexOf('{id}') !== -1){
      showToast('An ID is required for this operation', 'error');
      return;
    }

    var method = apiDef.m;
    var apiCall;
    if(method === 'POST') apiCall = API.post(path, fields);
    else if(method === 'PATCH') apiCall = API.patch(path, fields);
    else { showToast('Unsupported method', 'error'); return; }

    var btn = overlay.querySelector('.modal-ftr .btn-primary');
    if(btn){ btn.disabled = true; btn.textContent = 'Saving...'; }

    apiCall.then(function(result){
      showToast('Operation completed successfully', 'success');
      closeModal(id);
      if(typeof window.onModalSuccess === 'function'){
        window.onModalSuccess(id, result);
      }
    }).catch(function(err){
      showToast(err.message || 'Operation failed', 'error');
      if(btn){ btn.disabled = false; btn.textContent = 'Submit'; }
    });
  };

  /* ---------- picker API ---------- */
  function loadPickerData(type, query){
    var src = PICKER_SOURCES[type];
    if(!src) return;
    _pickerLoading = true;
    var list = document.getElementById('picker-list');
    if(list) list.innerHTML = '<div class="picker-empty"><div class="loader-spin" style="margin:0 auto"></div><p>Loading...</p></div>';

    var apiCall;
    if(src.search === 'GET'){
      var qPath = src.path + (query ? '?keyword='+encodeURIComponent(query) : '');
      apiCall = API.get(qPath);
    } else {
      var body = JSON.parse(JSON.stringify(src.body || {}));
      if(query) body.keyword = query;
      apiCall = src.search === 'POST' ? API.post(src.path, body) : API.get(src.path);
    }

    apiCall.then(function(data){
      PIC_DATA[type] = src.map(data);
      _pickerLoading = false;
      renderPickerItems(type, query || '');
    }).catch(function(){
      _pickerLoading = false;
      var list = document.getElementById('picker-list');
      if(list) list.innerHTML = '<div class="picker-empty"><p>Failed to load data</p></div>';
    });
  }

  window.openPicker = function(fieldId, type){
    _pickerField = fieldId;
    _pickerType = type;
    var names = {patient:'Patient',doctor:'Doctor',department:'Department',ward:'Ward',bed:'Bed',appointment:'Appointment',lab:'Lab Request',record:'Medical Record',admission:'Admission'};
    document.getElementById('picker-title').textContent = names[type] || type;
    document.getElementById('picker-search-input').value = '';
    document.getElementById('picker-overlay').classList.add('open');
    document.body.style.overflow='hidden';
    loadPickerData(type, '');
    setTimeout(function(){ document.getElementById('picker-search-input').focus(); }, 250);
  };
  window.closePicker = function(){
    document.getElementById('picker-overlay').classList.remove('open');
    document.body.style.overflow='';
    _pickerField = null;
  };
  window.filterPicker = function(query){
    if(!PIC_DATA[_pickerType] || PIC_DATA[_pickerType].length === 0){
      loadPickerData(_pickerType, query);
    } else {
      renderPickerItems(_pickerType, query);
    }
  };
  window.handlePickerSelect = function(id){
    var field = document.getElementById(_pickerField);
    if(field){ field.value = id; field.dispatchEvent(new Event('input',{bubbles:true})); }
    closePicker();
  };
})();
