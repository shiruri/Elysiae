/* Elysiae Modal System — all modals with DTO-matched fields */
(function(){
  'use strict';

  /* ---------- helpers ---------- */
  function esc(s){ return (s||'').replace(/&/g,'&amp;').replace(/"/g,'&quot;').replace(/</g,'&lt;'); }

  function input(id, label, type, attrs, col){
    var a = attrs || '';
    var cls = col === 'full' ? 'form-group' : 'form-group';
    return '<div class="'+cls+'">'
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

  /* ---------- modal definitions ---------- */
  var modals = [

    /* PATIENT — Create (PatientCreateRequest) */
    ['modal-patient-create', 'Register Patient',
      input('p-username','Username','text','required minlength="3" maxlength="80" placeholder="patient_john"')
      + input('p-firstName','First Name','text','required maxlength="100" placeholder="John"')
      + input('p-lastName','Last Name','text','required maxlength="100" placeholder="Doe"')
      + input('p-dateOfBirth','Date of Birth','date','required')
      + '<div class="form-row">'
        + select('p-gender','Gender',[{v:'',l:'Select...'},{v:'MALE',l:'Male'},{v:'FEMALE',l:'Female'},{v:'OTHER',l:'Other'}])
        + select('p-bloodType','Blood Type',[{v:'',l:'Select...'},{v:'A+',l:'A+'},{v:'A-',l:'A-'},{v:'B+',l:'B+'},{v:'B-',l:'B-'},{v:'AB+',l:'AB+'},{v:'AB-',l:'AB-'},{v:'O+',l:'O+'},{v:'O-',l:'O-'}])
      + '</div>'
      + input('p-phone','Phone','tel','pattern="\\+?[0-9]{10,15}" placeholder="+639123456789"')
      + input('p-email','Email','email','maxlength="120" placeholder="john@email.com"')
      + input('p-address','Address','text','placeholder="123 Main St"')
      + '<div class="form-row">'
        + input('p-ecName','Emergency Contact Name','text','placeholder="Jane Doe"')
        + input('p-ecPhone','Emergency Contact Phone','tel','pattern="\\+?[0-9]{10,15}" placeholder="+639987654321"')
      + '</div>',
      FTR],

    /* PATIENT — Update (PatientUpdateRequest) */
    ['modal-patient-update', 'Update Patient',
      input('pu-firstName','First Name','text','maxlength="100" placeholder="John"')
      + input('pu-lastName','Last Name','text','maxlength="100" placeholder="Doe"')
      + input('pu-dateOfBirth','Date of Birth','date','')
      + '<div class="form-row">'
        + select('pu-gender','Gender',[{v:'',l:'Select...'},{v:'MALE',l:'Male'},{v:'FEMALE',l:'Female'},{v:'OTHER',l:'Other'}])
        + select('pu-bloodType','Blood Type',[{v:'',l:'Select...'},{v:'A+',l:'A+'},{v:'A-',l:'A-'},{v:'B+',l:'B+'},{v:'B-',l:'B-'},{v:'AB+',l:'AB+'},{v:'AB-',l:'AB-'},{v:'O+',l:'O+'},{v:'O-',l:'O-'}])
      + '</div>'
      + input('pu-phone','Phone','tel','pattern="\\+?[0-9]{10,15}" placeholder="+639123456789"')
      + input('pu-email','Email','email','maxlength="120" placeholder="john@email.com"')
      + input('pu-address','Address','text','placeholder="123 Main St"')
      + '<div class="form-row">'
        + input('pu-ecName','Emergency Contact Name','text','placeholder="Jane Doe"')
        + input('pu-ecPhone','Emergency Contact Phone','tel','pattern="\\+?[0-9]{10,15}" placeholder="+639987654321"')
      + '</div>',
      FTR],

    /* DOCTOR — Create (DoctorCreateRequest) */
    ['modal-doctor-create', 'Register Doctor',
      input('d-username','Username','text','required minlength="3" maxlength="80" placeholder="dr_smith"')
      + input('d-departmentId','Department ID','number','required min="1" placeholder="1"')
      + input('d-firstName','First Name','text','required maxlength="100" placeholder="John"')
      + input('d-lastName','Last Name','text','required maxlength="100" placeholder="Smith"')
      + input('d-specialization','Specialization','text','maxlength="150" placeholder="Cardiology"')
      + input('d-licenseNumber','License Number','text','maxlength="80" placeholder="LIC-001"')
      + input('d-phone','Phone','tel','maxlength="20" placeholder="+639123456789"'),
      FTR],

    /* DOCTOR — Update (DoctorUpdateRequest) */
    ['modal-doctor-update', 'Update Doctor',
      input('du-departmentId','Department ID','number','min="1" placeholder="1"')
      + input('du-firstName','First Name','text','maxlength="100" placeholder="John"')
      + input('du-lastName','Last Name','text','maxlength="100" placeholder="Smith"')
      + input('du-specialization','Specialization','text','maxlength="150" placeholder="Cardiology"')
      + input('du-licenseNumber','License Number','text','maxlength="80" placeholder="LIC-001"')
      + input('du-phone','Phone','tel','maxlength="20" placeholder="+639123456789"'),
      FTR],

    /* DOCTOR — Schedule Update (DoctorScheduleUpdateRequest) */
    ['modal-doctor-schedule', 'Update Doctor Schedule',
      select('ds-dayOfWeek','Day of Week',[{v:'MON',l:'Monday'},{v:'TUE',l:'Tuesday'},{v:'WED',l:'Wednesday'},{v:'THU',l:'Thursday'},{v:'FRI',l:'Friday'},{v:'SAT',l:'Saturday'},{v:'SUN',l:'Sunday'}],'required')
      + '<div class="form-row">'
        + input('ds-startTime','Start Time','time','required')
        + input('ds-endTime','End Time','time','required')
      + '</div>'
      + input('ds-slotDurationMinutes','Slot Duration (minutes)','number','min="5" max="120" placeholder="30"'),
      FTR],

    /* DEPARTMENT — Create (DepartmentCreateRequest) */
    ['modal-dept-create', 'Create Department',
      input('dept-name','Department Name','text','required maxlength="100" placeholder="Cardiology"')
      + input('dept-floor','Floor','text','maxlength="20" placeholder="2nd Floor"'),
      FTR],

    /* DEPARTMENT — Update (DepartmentUpdateRequest) */
    ['modal-dept-update', 'Update Department',
      input('deptu-id','Department ID','number','required min="1" placeholder="1"')
      + input('deptu-name','Department Name','text','maxlength="100" placeholder="Cardiology Updated"')
      + input('deptu-floor','Floor','text','maxlength="20" placeholder="3rd Floor"'),
      FTR],

    /* WARD — Create (WardCreateRequest) */
    ['modal-ward-create', 'Create Ward',
      input('w-name','Ward Name','text','required maxlength="100" placeholder="General Ward"')
      + select('w-type','Ward Type',[{v:'GENERAL',l:'General'},{v:'ICU',l:'ICU'},{v:'PRIVATE',l:'Private'},{v:'PEDIATRIC',l:'Pediatric'},{v:'MATERNITY',l:'Maternity'},{v:'ISOLATION',l:'Isolation'},{v:'EMERGENCY',l:'Emergency'}],'required')
      + input('w-floor','Floor','text','maxlength="20" placeholder="1st"'),
      FTR],

    /* BED — Add (BedAddRequest) */
    ['modal-bed-create', 'Add Bed',
      input('b-wardId','Ward ID','number','required min="1" placeholder="1"')
      + input('b-bedNo','Bed Number','text','required minlength="1" maxlength="20" placeholder="A1"')
      + select('b-status','Status',[{v:'AVAILABLE',l:'Available'},{v:'OCCUPIED',l:'Occupied'},{v:'MAINTENANCE',l:'Maintenance'},{v:'RESERVED',l:'Reserved'}],'required'),
      FTR],

    /* ADMISSION — Admit (BedAdmitPatientRequest) */
    ['modal-admission-create', 'Admit Patient',
      input('adm-patientId','Patient ID','number','required min="1" placeholder="1"')
      + input('adm-bedId','Bed ID','number','required min="1" placeholder="1"')
      + input('adm-doctorId','Doctor ID','number','required min="1" placeholder="1"'),
      FTR],

    /* ADMISSION — Transfer (AdmissionTransferRequest) */
    ['modal-admission-transfer', 'Transfer Patient',
      input('atf-patientId','Patient ID','number','required min="1" placeholder="1"')
      + input('atf-newBedId','New Bed ID','number','required min="1" placeholder="2"')
      + input('atf-newDoctorId','New Doctor ID (optional)','number','min="1" placeholder="2"'),
      FTR],

    /* APPOINTMENT — Create (AppointmentCreateRequest) */
    ['modal-appt-create', 'Create Appointment',
      input('ap-patientId','Patient ID','number','required min="1" placeholder="1"')
      + input('ap-doctorId','Doctor ID','number','required min="1" placeholder="1"')
      + input('ap-appointmentDateTime','Date & Time','datetime-local','required')
      + select('ap-type','Type',[{v:'CONSULTATION',l:'Consultation'},{v:'FOLLOW_UP',l:'Follow Up'},{v:'EMERGENCY',l:'Emergency'},{v:'CHECKUP',l:'Checkup'},{v:'PROCEDURE',l:'Procedure'}]),
      FTR],

    /* APPOINTMENT — Update (AppointmentUpdateRequest) */
    ['modal-appt-update', 'Update Appointment',
      select('apu-type','Type',[{v:'',l:'Select...'},{v:'CONSULTATION',l:'Consultation'},{v:'FOLLOW_UP',l:'Follow Up'},{v:'EMERGENCY',l:'Emergency'},{v:'CHECKUP',l:'Checkup'},{v:'PROCEDURE',l:'Procedure'}])
      + input('apu-appointmentDate','New Date & Time','datetime-local','')
      + textarea('apu-notes','Notes','placeholder="Follow-up visit notes..."'),
      FTR],

    /* LAB — Create Request (LabRequestCreateRequest) */
    ['modal-lab-create', 'Create Lab Request',
      input('lab-patientId','Patient ID','number','required min="1" placeholder="1"')
      + input('lab-doctorId','Doctor ID','number','required min="1" placeholder="1"')
      + input('lab-testType','Test Type','text','required maxlength="150" placeholder="BLOOD_TEST"')
      + select('lab-priority','Priority',[{v:'',l:'Routine (default)'},{v:'ROUTINE',l:'Routine'},{v:'URGENT',l:'Urgent'},{v:'STAT',l:'STAT'}]),
      FTR],

    /* LAB — Post Result (LabResultCreateRequest) */
    ['modal-lab-result', 'Post Lab Result',
      input('lr-labRequestId','Lab Request ID','number','required min="1" placeholder="1"')
      + input('lr-resultValue','Result Value','text','required placeholder="Normal"')
      + input('lr-normalRange','Normal Range','text','placeholder="0-100"')
      + '<div class="form-row">'
        + select('lr-isAbnormal','Is Abnormal',[{v:'false',l:'No'},{v:'true',l:'Yes'}])
        + input('lr-remarks','Remarks','text','placeholder="All clear"')
      + '</div>',
      FTR],

    /* VITALS — Create (VitalsCreateRequest) */
    ['modal-vitals-create', 'Log Vitals',
      input('v-patientId','Patient ID','number','required min="1" placeholder="1"')
      + '<div class="form-row">'
        + input('v-temperature','Temperature (°C)','number','step="0.1" placeholder="37.2"')
        + input('v-bloodPressure','Blood Pressure','text','placeholder="120/80"')
      + '</div>'
      + '<div class="form-row">'
        + input('v-heartRate','Heart Rate (bpm)','number','min="0" placeholder="72"')
        + input('v-oxygenSat','Oxygen Saturation (%)','number','step="0.1" placeholder="98.5"')
      + '</div>'
      + '<div class="form-row">'
        + input('v-weightKg','Weight (kg)','number','step="0.1" placeholder="70.5"')
        + input('v-heightCm','Height (cm)','number','step="0.1" placeholder="175.0"')
      + '</div>',
      FTR],

    /* MEDICAL RECORD — Create (MedicalRecordCreateRequest) */
    ['modal-record-create', 'Create Medical Record',
      input('mr-patientId','Patient ID','number','required min="1" placeholder="1"')
      + input('mr-doctorId','Doctor ID','number','required min="1" placeholder="1"')
      + '<div class="form-row">'
        + input('mr-appointmentId','Appointment ID (optional)','number','min="1" placeholder="1"')
        + input('mr-admissionId','Admission ID (optional)','number','min="1" placeholder="1"')
      + '</div>'
      + textarea('mr-diagnosis','Diagnosis','placeholder="Influenza A"')
      + textarea('mr-notes','Notes','placeholder="Rest and fluids recommended"'),
      FTR],

    /* MEDICAL RECORD — Update (MedicalRecordUpdateRequest) */
    ['modal-record-update', 'Update Medical Record',
      input('mru-recordId','Record ID','number','required min="1" placeholder="1"')
      + textarea('mru-diagnosis','Diagnosis','placeholder="Updated diagnosis"')
      + textarea('mru-notes','Notes','placeholder="Updated notes"'),
      FTR],

    /* PRESCRIPTION — Create (PrescriptionCreateRequest) */
    ['modal-prescription-create', 'Add Prescription',
      input('rx-recordId','Medical Record ID','number','required min="1" placeholder="1"')
      + input('rx-medicineName','Medicine Name','text','required maxlength="200" placeholder="Paracetamol 500mg"')
      + '<div class="form-row">'
        + input('rx-dosage','Dosage','text','maxlength="100" placeholder="500mg"')
        + input('rx-frequency','Frequency','text','maxlength="100" placeholder="3x/day"')
      + '</div>'
      + input('rx-durationDays','Duration (days)','number','min="1" placeholder="7"'),
      FTR],

    /* USER — Create (UserCreateRequest) */
    ['modal-user-create', 'Create User',
      input('u-username','Username','text','required minlength="6" maxlength="100" placeholder="newuser"')
      + select('u-role','Role',[{v:'ADMIN',l:'Admin'},{v:'DOCTOR',l:'Doctor'},{v:'NURSE',l:'Nurse'},{v:'PATIENT',l:'Patient'},{v:'RECEPTIONIST',l:'Receptionist'},{v:'PHARMACIST',l:'Pharmacist'},{v:'LAB_TECH',l:'Lab Tech'},{v:'CASHIER',l:'Cashier'}],'required'),
      FTR],

    /* USER — Update (UserUpdateRequest) */
    ['modal-user-update', 'Update User',
      input('uu-username','Username','text','minlength="6" maxlength="100" placeholder="updateduser"')
      + select('uu-role','Role',[{v:'',l:'Select...'},{v:'ADMIN',l:'Admin'},{v:'DOCTOR',l:'Doctor'},{v:'NURSE',l:'Nurse'},{v:'PATIENT',l:'Patient'},{v:'RECEPTIONIST',l:'Receptionist'},{v:'PHARMACIST',l:'Pharmacist'},{v:'LAB_TECH',l:'Lab Tech'},{v:'CASHIER',l:'Cashier'}]),
      FTR],

    /* USER — Change Password (UserChangePasswordRequest) */
    ['modal-user-password', 'Change Password',
      input('cp-oldPassword','Old Password','password','required maxlength="255"')
      + input('cp-newPassword','New Password','password','required maxlength="255"'),
      FTR],
  ];

  /* ---------- inject ---------- */
  function init(){
    var html = '';
    modals.forEach(function(m){
      html += modalHTML(m[0], m[1], m[2], m[3]);
    });
    document.insertAdjacentHTML('beforeend', html);

    /* click outside to close */
    document.addEventListener('click', function(e){
      if(e.target.classList.contains('modal-overlay')){
        closeModal(e.target.id);
      }
    });

    /* Escape key to close */
    document.addEventListener('keydown', function(e){
      if(e.key === 'Escape'){
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

  /* ---------- global API ---------- */
  window.openModal = function(id){
    var el = document.getElementById(id);
    if(el){ el.classList.add('active'); document.body.style.overflow='hidden'; }
  };
  window.closeModal = function(id){
    var el = typeof id === 'string' ? document.getElementById(id) : document.getElementById(id);
    if(el){ el.classList.remove('active'); document.body.style.overflow=''; }
  };
  window.handleModalSubmit = function(id){
    var overlay = document.getElementById(id);
    if(!overlay) return;

    /* Collect all form fields */
    var fields = {};
    var inputs = overlay.querySelectorAll('input, select, textarea');
    inputs.forEach(function(inp){
      if(inp.id){
        var key = inp.id.replace(/^[a-z]+-/, ''); /* strip prefix */
        var val = inp.value;
        /* type coercion */
        if(inp.type === 'number' && val !== '') val = Number(val);
        else if(inp.type === 'datetime-local' && val !== '') val = val;
        else if(val === '') val = null;
        else if(val === 'true') val = true;
        else if(val === 'false') val = false;
        fields[key] = val;
      }
    });

    console.log('[Modal Submit]', id, JSON.stringify(fields, null, 2));
    showToast('Form submitted (check console for payload)', 'success');
    closeModal(id);
  };
})();
