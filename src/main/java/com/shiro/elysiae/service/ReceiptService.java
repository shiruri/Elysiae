package com.shiro.elysiae.service;

import com.shiro.elysiae.exception.AppException;
import com.shiro.elysiae.exception.ErrorCode;
import com.shiro.elysiae.model.User;
import com.shiro.elysiae.model.doctorsndepartment.Doctor;
import com.shiro.elysiae.model.enums.AuditAction;
import com.shiro.elysiae.model.enums.Role;
import com.shiro.elysiae.model.patient.Patient;
import com.shiro.elysiae.repository.DoctorRepository;
import com.shiro.elysiae.repository.PatientRepository;
import com.shiro.elysiae.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String reprintCredentialSlip(Long id, Role role) {
        if (role.name().equals("PATIENT")) {
            Patient patient = patientRepository.findById(id)
                    .orElseThrow(() -> new AppException(ErrorCode.PATIENT_NOT_FOUND));
            String tempPassword = patient.getUser().getUsername() + "-" + (1000 + new Random().nextInt(9000));

            User user = patient.getUser();
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setMustChangePassword(true);
            userRepository.save(user);

            auditService.log(String.valueOf(AuditAction.PATIENT_CREDENTIAL_RESET), user.getUsername(), Long.valueOf(String.valueOf(id)));
            return generatePatientCredentialSlip(patient, tempPassword);

        } else if (role.name().equals("DOCTOR")) {
            Doctor doctor = doctorRepository.findById(id)
                    .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
            String tempPassword = doctor.getUser().getUsername() + "-" + (1000 + new Random().nextInt(9000));

            User user = doctor.getUser();
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setMustChangePassword(true);
            userRepository.save(user);

            auditService.log(String.valueOf(AuditAction.DOCTOR_CREDENTIAL_RESET), user.getUsername(), Long.valueOf(String.valueOf(id)));
            return generateDoctorCredentialSlip(doctor, tempPassword);

        } else {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            String tempPassword = user.getUsername() + "-" + (1000 + new Random().nextInt(9000));

            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setMustChangePassword(true);
            userRepository.save(user);

            auditService.log(String.valueOf(AuditAction.STAFF_CREDENTIAL_RESET), user.getUsername(), Long.valueOf(String.valueOf(id)));
            return generateStaffCredentialSlip(user, tempPassword);
        }
    }


    public String generatePatientCredentialSlip(Patient patient, String tempPassword) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8">
                  <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                      width: 80mm;
                      font-family: 'Courier New', monospace;
                      font-size: 11px;
                      padding: 8px;
                      background: #fff;
                      color: #222;
                    }
                    .header-band {
                      background: #e8f4f0;
                      border: 1px solid #b2d8cc;
                      border-radius: 4px;
                      padding: 6px 8px;
                      margin-bottom: 6px;
                      text-align: center;
                    }
                    .hospital { font-size: 14px; font-weight: bold; color: #2e7d64; letter-spacing: 1px; }
                    .slip-type { font-size: 9px; color: #5a9e89; margin-top: 1px; text-transform: uppercase; letter-spacing: 2px; }
                    .divider-dots { border: none; border-top: 1px dotted #aac; margin: 5px 0; }
                    .divider-solid { border: none; border-top: 1px solid #ccc; margin: 5px 0; }
                    .row { display: flex; justify-content: space-between; margin: 3px 0; }
                    .label { font-size: 9px; color: #777; text-transform: uppercase; letter-spacing: 0.5px; }
                    .value { font-size: 11px; color: #111; text-align: right; max-width: 55mm; word-break: break-all; }
                    .value.mono { font-family: 'Courier New', monospace; font-weight: bold; }
                    .pw-box {
                      background: #f0faf6;
                      border: 1px dashed #7cbfaa;
                      border-radius: 3px;
                      padding: 5px 8px;
                      margin: 6px 0;
                      text-align: center;
                    }
                    .pw-label { font-size: 9px; color: #5a9e89; text-transform: uppercase; letter-spacing: 1px; }
                    .pw-value { font-size: 13px; font-weight: bold; letter-spacing: 2px; color: #1a5c47; margin-top: 2px; }
                    .note {
                      font-size: 8.5px;
                      font-style: italic;
                      color: #888;
                      text-align: center;
                      margin-top: 7px;
                      line-height: 1.5;
                    }
                    .footer-tag {
                      text-align: center;
                      font-size: 8px;
                      color: #aaa;
                      margin-top: 5px;
                      letter-spacing: 1px;
                    }
                    @media print {
                      @page { margin: 0; size: 80mm auto; }
                      body  { width: 80mm; }
                    }
                  </style>
                </head>
                <body>
                  <div class="header-band">
                    <div class="hospital">&#x2665; Elysiae Hospital</div>
                    <div class="slip-type">Patient Access Credentials</div>
                  </div>

                  <div class="row">
                    <span class="label">Patient Name</span>
                    <span class="value">%s %s</span>
                  </div>
                  <div class="row">
                    <span class="label">Username</span>
                    <span class="value mono">%s</span>
                  </div>
                  <div class="row">
                    <span class="label">Date Issued</span>
                    <span class="value">%s</span>
                  </div>

                  <hr class="divider-dots"/>

                  <div class="pw-box">
                    <div class="pw-label">Temporary Password</div>
                    <div class="pw-value">%s</div>
                  </div>

                  <hr class="divider-solid"/>
                  <div class="note">
                    Change your password upon first login.<br>
                    Keep this slip in a safe place.
                  </div>
                  <div class="footer-tag">— PATIENT COPY —</div>
                </body>
                </html>
                """.formatted(
                patient.getFirstName(),
                patient.getLastName(),
                patient.getUser().getUsername(),
                LocalDate.now(),
                tempPassword
        );
    }

    public String generateDoctorCredentialSlip(Doctor doctor, String tempPassword) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8">
                  <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                      width: 80mm;
                      font-family: 'Courier New', monospace;
                      font-size: 11px;
                      background: #fff;
                      color: #111;
                    }
                    .header {
                      background: #1a1a2e;
                      color: #fff;
                      padding: 8px 10px;
                      text-align: center;
                    }
                    .hospital { font-size: 14px; font-weight: bold; letter-spacing: 2px; text-transform: uppercase; }
                    .slip-type {
                      font-size: 8px;
                      letter-spacing: 3px;
                      text-transform: uppercase;
                      color: #a0b4d0;
                      margin-top: 2px;
                    }
                    .accent-bar { height: 3px; background: linear-gradient(to right, #4a90d9, #1a1a2e); }
                    .body { padding: 8px 10px; }
                    .section-title {
                      font-size: 8px;
                      text-transform: uppercase;
                      letter-spacing: 2px;
                      color: #4a90d9;
                      margin: 6px 0 3px;
                      border-bottom: 1px solid #e0e8f0;
                      padding-bottom: 2px;
                    }
                    .field { display: flex; justify-content: space-between; margin: 3px 0; }
                    .label { font-size: 9px; color: #888; }
                    .value { font-size: 11px; font-weight: bold; text-align: right; }
                    .value.mono { letter-spacing: 1px; }
                    .pw-block {
                      background: #1a1a2e;
                      color: #fff;
                      border-radius: 3px;
                      padding: 6px 8px;
                      margin: 8px 0 4px;
                      text-align: center;
                    }
                    .pw-label { font-size: 8px; letter-spacing: 2px; text-transform: uppercase; color: #a0b4d0; }
                    .pw-value { font-size: 14px; font-weight: bold; letter-spacing: 3px; color: #fff; margin-top: 3px; }
                    .divider { border: none; border-top: 1px solid #ddd; margin: 5px 0; }
                    .note {
                      font-size: 8.5px;
                      color: #999;
                      text-align: center;
                      font-style: italic;
                      line-height: 1.5;
                      margin-top: 5px;
                    }
                    .footer {
                      background: #f5f7fa;
                      border-top: 1px solid #ddd;
                      padding: 4px;
                      text-align: center;
                      font-size: 8px;
                      color: #aaa;
                      letter-spacing: 1.5px;
                      text-transform: uppercase;
                      margin-top: 6px;
                    }
                    @media print {
                      @page { margin: 0; size: 80mm auto; }
                      body  { width: 80mm; }
                    }
                  </style>
                </head>
                <body>
                  <div class="header">
                    <div class="hospital">Elysiae Hospital</div>
                    <div class="slip-type">Physician System Access</div>
                  </div>
                  <div class="accent-bar"></div>

                  <div class="body">
                    <div class="section-title">Physician</div>
                    <div class="field">
                      <span class="label">Full Name</span>
                      <span class="value">Dr. %s %s</span>
                    </div>
                    <div class="field">
                      <span class="label">Department</span>
                      <span class="value">%s</span>
                    </div>

                    <div class="section-title">Access Credentials</div>
                    <div class="field">
                      <span class="label">Username</span>
                      <span class="value mono">%s</span>
                    </div>
                    <div class="field">
                      <span class="label">Date Issued</span>
                      <span class="value">%s</span>
                    </div>

                    <div class="pw-block">
                      <div class="pw-label">Temporary Password</div>
                      <div class="pw-value">%s</div>
                    </div>

                    <hr class="divider"/>
                    <div class="note">
                      Mandatory password reset required on first login.<br>
                      Do not share or reproduce this document.
                    </div>
                  </div>
                  <div class="footer">— Physician Confidential —</div>
                </body>
                </html>
                """.formatted(
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getDepartment() != null ? doctor.getDepartment().getName() : "N/A",
                doctor.getUser().getUsername(),
                LocalDate.now(),
                tempPassword
        );
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // STAFF SLIP — Minimal utilitarian monochrome style
    // ─────────────────────────────────────────────────────────────────────────────
    public String generateStaffCredentialSlip(User user, String tempPassword) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8">
                  <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                      width: 80mm;
                      font-family: 'Courier New', monospace;
                      font-size: 11px;
                      padding: 8px;
                      background: #fff;
                      color: #000;
                    }
                    .top-rule { border: none; border-top: 3px double #000; margin-bottom: 5px; }
                    .bot-rule { border: none; border-top: 3px double #000; margin-top: 5px; }
                    .hospital {
                      font-size: 13px;
                      font-weight: bold;
                      text-transform: uppercase;
                      letter-spacing: 3px;
                      text-align: center;
                    }
                    .slip-type {
                      font-size: 8px;
                      text-transform: uppercase;
                      letter-spacing: 3px;
                      text-align: center;
                      color: #555;
                      margin-bottom: 6px;
                    }
                    .divider { border: none; border-top: 1px solid #000; margin: 5px 0; }
                    .row { display: flex; margin: 3px 0; }
                    .label { font-size: 9px; text-transform: uppercase; letter-spacing: 0.5px; width: 28mm; color: #555; flex-shrink: 0; }
                    .value { font-size: 11px; font-weight: bold; }
                    .value.mono { letter-spacing: 1px; }
                    .pw-row { margin: 6px 0; }
                    .pw-label { font-size: 9px; text-transform: uppercase; letter-spacing: 1px; color: #555; }
                    .pw-value {
                      font-size: 15px;
                      font-weight: bold;
                      letter-spacing: 4px;
                      border: 1px solid #000;
                      display: inline-block;
                      padding: 2px 6px;
                      margin-top: 2px;
                    }
                    .note {
                      font-size: 8px;
                      color: #666;
                      font-style: italic;
                      text-align: center;
                      line-height: 1.5;
                      margin-top: 5px;
                    }
                    .tag {
                      font-size: 8px;
                      text-align: center;
                      letter-spacing: 2px;
                      text-transform: uppercase;
                      color: #aaa;
                      margin-top: 4px;
                    }
                    @media print {
                      @page { margin: 0; size: 80mm auto; }
                      body  { width: 80mm; }
                    }
                  </style>
                </head>
                <body>
                  <hr class="top-rule"/>
                  <div class="hospital">Elysiae Hospital</div>
                  <div class="slip-type">Staff System Access Slip</div>
                  <hr class="divider"/>

                  <div class="row">
                    <span class="label">Account</span>
                    <span class="value mono">%s</span>
                  </div>
                  <div class="row">
                    <span class="label">Role</span>
                    <span class="value">%s</span>
                  </div>
                  <div class="row">
                    <span class="label">Issued</span>
                    <span class="value">%s</span>
                  </div>

                  <hr class="divider"/>

                  <div class="pw-row">
                    <div class="pw-label">Temp. Password</div>
                    <div class="pw-value">%s</div>
                  </div>

                  <hr class="divider"/>
                  <div class="note">
                    Reset password on first login. Internal use only.
                  </div>
                  <div class="tag">[ Staff Copy ]</div>
                  <hr class="bot-rule"/>
                </body>
                </html>
                """.formatted(
                user.getUsername(),
                user.getRole() != null ? user.getRole().name() : "STAFF",
                LocalDate.now(),
                tempPassword
        );
    }
}