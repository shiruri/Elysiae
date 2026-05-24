# 🏥 Elysiae Hospital Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![MapStruct](https://img.shields.io/badge/MapStruct-1.5+-red?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

**A full-featured, role-based Hospital Management System REST API built with Spring Boot.**

*Manage patients, doctors, appointments, wards, billing, pharmacy, lab requests, and more — all secured with JWT authentication.*

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Domain Modules](#-domain-modules)
- [Security & Authentication](#-security--authentication)
- [API Endpoints](#-api-endpoints)
- [Roles & Permissions](#-roles--permissions)
- [Data Models](#-data-models)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [Credential Slip System](#-credential-slip-system)

---

## 🌟 Overview

**Elysiae** is a comprehensive Hospital Management System backend designed to streamline operations across all hospital departments. Built as a RESTful API with Spring Boot, it provides a unified platform for managing the entire patient lifecycle — from registration and appointment booking through clinical care, laboratory testing, pharmacy dispensing, and final billing.

The system enforces strict role-based access control, full audit logging, and soft-deletion patterns to ensure data integrity and compliance.

---

## ✨ Features

### 👤 User & Auth Management
- JWT-based stateless authentication with role-based authorization
- Mandatory first-login password change enforcement
- Temporary password generation with printable credential slips (HTML receipt format)
- Rate limiting on login and password-change endpoints (5 requests/minute per IP)
- Soft deletion for users, patients, doctors, and departments
- Full audit trail on all user actions

### 🧑‍⚕️ Patient Management
- Register patients with auto-generated login credentials
- Search patients by name, gender, blood type, and age range
- View patient medical records, appointment history, and invoices
- Self-service patient portal (`/me` endpoints)
- Printable credential slips for new patients

### 👨‍⚕️ Doctor Management
- Register doctors with department assignment and license validation
- Update doctor profiles and specialization
- Weekly schedule management with configurable slot durations
- View upcoming assigned patients and appointment queues
- Doctor-specific credential slip generation

### 📅 Appointment System
- Book, update, and cancel appointments
- Check available time slots based on doctor schedule
- Status management: `SCHEDULED → COMPLETED / CANCELLED / NO_SHOW`
- Patients restricted to cancellation only; staff can update any status
- Per-patient and per-doctor appointment history with pagination

### 🏥 Ward & Bed Management
- Create wards by type: `GENERAL`, `ICU`, `PEDIATRIC`, `MATERNITY`, `SURGICAL`
- Add beds to wards with status tracking: `AVAILABLE`, `OCCUPIED`, `MAINTENANCE`
- Bed occupancy reports per ward with occupancy rate percentage
- Cascading soft-delete (deleting a ward soft-deletes all its beds)

### 🛏️ Admission System
- Admit patients to available beds with an attending doctor
- Transfer patients to new beds (optionally reassigning doctor)
- Discharge patients with a final diagnosis
- Full admission history per patient and per doctor

### 🧪 Laboratory
- Create lab requests with priority levels: `ROUTINE`, `URGENT`, `STAT`
- Status tracking: `PENDING → IN_PROGRESS → COMPLETED`
- Post lab results with normal range, abnormality flag, and remarks
- Lab technicians automatically scoped to their own queue
- Doctors scoped to their own patients' lab requests

### 💊 Pharmacy
- Medicine inventory with stock tracking and reorder level alerts
- Low-stock and expiry-date filters
- Dispense medicines against prescriptions
- Prevents double-dispensing and dispensing expired medicines
- Dispense log with full audit trail

### 🧾 Electronic Health Records (EHR)
- Create medical records linked to appointments or admissions
- Add diagnoses, clinical notes, and prescriptions
- Prescription management tied to medical records
- Patient and doctor scoped access controls

### 📊 Vitals Tracking
- Log patient vitals: temperature, blood pressure, heart rate, oxygen saturation, weight, height
- Time-series history per patient with pagination
- Logged by the attending nurse or doctor

### 💰 Billing & Invoicing
- Auto-generate invoices from admissions using configurable service rates
- Add line items by category: `CONSULTATION`, `LAB`, `MEDICINE`, `BED`, `PROCEDURE`
- Unit prices auto-resolved from service rate tables or medicine catalog
- Record partial and full payments via `CASH`, `CARD`, `INSURANCE`, `GCASH`
- Invoice status auto-updates: `UNPAID → PARTIAL → PAID`
- Revenue reports and dashboard metrics for admins

### 🏢 Department Management
- Create and manage hospital departments with floor information
- List all doctors per department
- Soft-delete departments (preserves historical data)

### 📜 Audit Logging
- Every significant action is logged: logins, patient operations, clinical events, billing
- Filterable by user ID, action type, and date range
- Admin-only access to audit log

### 📈 Admin Dashboard & Reports
- Real-time dashboard: patient count, doctor count, today's appointments, revenue
- Revenue reports by date range with paid/unpaid/partial breakdown
- Bed occupancy report across all wards

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot 3.x |
| Security | Spring Security + JWT (jjwt) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8.x |
| Mapping | MapStruct |
| Validation | Jakarta Bean Validation |
| Boilerplate | Lombok |
| Build Tool | Maven |
| Health Check | Spring Boot Actuator |

---

## 🏗 Architecture

Elysiae follows a classic **layered architecture** pattern:

```
┌─────────────────────────────────────────────────────────────┐
│                    HTTP Requests                             │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│            Security Layer (JWT Filter + Rate Limiter)        │
│   RateLimitingFilter → JwtAuthenticationFilter               │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                   Controller Layer                           │
│   @RestController  •  @PreAuthorize (method security)        │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                    Service Layer                             │
│   Business logic  •  Authorization validation  •  Auditing  │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                  Repository Layer                            │
│   Spring Data JPA  •  Custom JPQL queries                   │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                 Database (MySQL)                             │
│   JPA Entities  •  Indexed columns  •  Soft deletes         │
└─────────────────────────────────────────────────────────────┘
```

### Key Design Decisions

- **Soft Deletes** — Users, patients, doctors, departments, beds, and wards are never hard-deleted. A `deleted_at` timestamp is set instead, and all queries filter `WHERE deleted_at IS NULL`.
- **Audit Log** — Every mutating operation logs to `audit_logs` via `AuditService`, capturing the acting user, action type, affected entity, and timestamp.
- **DTO Pattern** — All API responses use records (`record` types) as DTOs. MapStruct mappers handle entity-to-DTO conversion cleanly at compile time.
- **Method Security** — `@PreAuthorize` annotations on every controller method enforce role-based access at the method level, backed by Spring Security's `@EnableMethodSecurity`.
- **Pagination** — All list endpoints accept Spring `Pageable` parameters (`page`, `size`, `sort`).

---

## 📁 Project Structure

```
src/main/java/com/shiro/elysiae/
├── ElysiaeApplication.java
│
├── config/
│   ├── RateLimitingFilter.java       # IP-based rate limiter for auth endpoints
│   └── SecurityConfig.java           # JWT filter, CORS, Spring Security chain
│
├── controller/
│   ├── AdmissionController.java
│   ├── AppointmentController.java
│   ├── AuditController.java
│   ├── AuthController.java
│   ├── BillingController.java
│   ├── DepartmentController.java
│   ├── DoctorController.java
│   ├── LabController.java
│   ├── PatientController.java
│   ├── PharmacyController.java
│   ├── ReceiptController.java
│   ├── RecordsController.java
│   ├── ReportController.java
│   ├── VitalsController.java
│   └── WardController.java
│
├── service/                          # Business logic layer
├── repository/                       # Spring Data JPA repositories
│
├── model/
│   ├── User.java
│   ├── appointments/
│   ├── audit/
│   ├── billing/
│   ├── doctorsndepartment/
│   ├── ehrnprescriptionsnvitals/
│   ├── enums/
│   ├── laborotory/
│   ├── patient/
│   ├── pharmacy/
│   └── wardsbedsadmission/
│
├── dto/
│   ├── request/                      # Inbound request records
│   └── response/                     # Outbound response records
│
├── exception/
│   ├── AppException.java
│   ├── ErrorCode.java                # Centralized error codes with HTTP status
│   └── GlobalExceptionHandler.java
│
└── util/
    ├── JwtUtils.java
    ├── *Mapper.java                  # MapStruct mappers
    └── ReceiptService.java           # HTML slip generation
```

---

## 📦 Domain Modules

| Module | Entities | Key Operations |
|---|---|---|
| **Auth/Users** | `User` | Login, register, password change, logout |
| **Patients** | `Patient` | CRUD, search, self-service portal |
| **Doctors** | `Doctor`, `DoctorSchedule` | CRUD, schedule management, slot availability |
| **Departments** | `Department` | CRUD, doctor listing |
| **Appointments** | `Appointment` | Book, update, cancel, slot check |
| **Wards & Beds** | `Ward`, `Bed` | Create, search, occupancy tracking |
| **Admissions** | `Admission` | Admit, discharge, transfer |
| **EHR** | `MedicalRecord`, `Prescription` | Create, update, add prescriptions |
| **Vitals** | `Vitals` | Log and retrieve patient vitals |
| **Laboratory** | `LabRequest`, `LabResult` | Request, update status, post results |
| **Pharmacy** | `Medicine`, `DispenseLog` | Inventory, dispensing |
| **Billing** | `Invoice`, `InvoiceItem`, `Payment`, `ServiceRate` | Generate, add items, record payments |
| **Audit** | `AuditLog` | Query audit trail |
| **Reports** | — | Dashboard, revenue, bed occupancy |

---

## 🔐 Security & Authentication

### JWT Token Structure

Every authenticated request requires a `Bearer` token in the `Authorization` header.

```
Authorization: Bearer <token>
```

The JWT payload contains:

```json
{
  "sub": "12",
  "role": "DOCTOR",
  "mustChangePassword": false,
  "iat": 1700000000,
  "exp": 1700086400
}
```

### Password Change Enforcement

If `mustChangePassword: true` is present in the token, **all API requests except `/api/auth/change-password/{id}`** are blocked with:

```json
{
  "error": "PASSWORD_CHANGE_REQUIRED",
  "message": "You must change your password before continuing",
  "status": 403
}
```

### Rate Limiting

`RateLimitingFilter` limits `/api/auth/login` and `/api/auth/change-password` to **5 requests per IP per minute**. Exceeding this returns HTTP `408`.

---

## 📡 API Endpoints

### Auth — `/api/auth`

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/login` | Public | Login and receive JWT |
| `GET` | `/me` | Authenticated | Get current user info |
| `POST` | `/register` | ADMIN | Create a new user account |
| `PATCH` | `/change-password/{id}` | Authenticated | Change password |
| `POST` | `/search-users` | ADMIN | Search users |
| `DELETE` | `/{id}` | ADMIN | Soft-delete user |
| `PATCH` | `/update/{id}` | Authenticated | Update username/role |
| `PATCH` | `/logout/{id}` | Authenticated | Logout (deactivate user) |

### Patients — `/api/patients`

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `` | RECEPTIONIST, ADMIN | Register patient + print slip |
| `POST` | `/get-patients` | Staff roles | Search patients |
| `GET` | `/{id}` | ADMIN, DOCTOR, RECEPTIONIST | Get patient details |
| `PATCH` | `/{id}` | Authenticated | Update patient |
| `GET` | `/{id}/medical-record` | Staff roles | Patient medical records |
| `GET` | `/{id}/invoice` | Staff roles | Patient invoices |
| `GET` | `/{id}/appointments` | Staff roles | Patient appointments |
| `GET` | `/me` | Authenticated | Current patient profile |
| `GET` | `/{id}/slip` | RECEPTIONIST, ADMIN | Reprint credential slip |
| `DELETE` | `/{id}` | ADMIN | Soft-delete patient |

### Doctors — `/api/doctor`

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/register` | ADMIN | Register new doctor |
| `POST` | `` | All clinical roles | Search doctors |
| `GET` | `/{id}` | All clinical roles | Get doctor details |
| `PATCH` | `/update/{id}` | ADMIN, DOCTOR | Update doctor |
| `PATCH` | `/update/{id}/schedule` | All roles | Update schedule |
| `POST` | `/{id}/schedule` | Clinical + Patient | View weekly schedule |
| `GET` | `/{id}/patients` | ADMIN, DOCTOR, NURSE | Assigned patients |
| `GET` | `/me/patients` | ADMIN, DOCTOR | Current doctor's patients |
| `DELETE` | `/{id}` | ADMIN | Soft-delete doctor |

### Appointments — `/api/appointments`

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/create` | ADMIN, PATIENT, RECEPTIONIST | Book appointment |
| `POST` | `` | Staff roles | Search appointments |
| `GET` | `/me` | Authenticated | My appointments |
| `GET` | `/{id}` | Staff roles | Get appointment |
| `GET` | `/me/{id}` | Authenticated | My specific appointment |
| `PATCH` | `/{id}` | Staff roles | Update appointment |
| `PATCH` | `/me/{id}` | Authenticated | Reschedule my appointment |
| `PATCH` | `/{id}/status` | Staff roles | Update status |
| `PATCH` | `/me/{id}/status` | Authenticated | Cancel my appointment |
| `GET` | `/slot` | Staff roles | Available slots |

### Wards — `/api/wards`

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/register/ward` | ADMIN | Create ward |
| `POST` | `/add/bed` | ADMIN | Add bed to ward |
| `POST` | `/search` | Clinical roles | Search wards |
| `GET` | `/beds/{id}` | Clinical roles | Beds in a ward |
| `GET` | `/bed/{id}` | Clinical roles | Bed details |
| `GET` | `/ward/{id}` | Clinical roles | Ward details |
| `DELETE` | `/ward/{id}` | ADMIN | Delete ward (cascade) |
| `DELETE` | `/bed/{id}` | ADMIN | Delete bed |

### Admissions — `/api/admission`

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/ward/admissions` | Clinical roles | Admit patient |
| `GET` | `/{doctorId}/doctor` | Clinical roles | Admissions by doctor |
| `GET` | `/{patientId}/patient` | All roles | Admissions by patient |
| `POST` | `/{id}` | Clinical roles | Admission details |
| `PATCH` | `/{patient}/discharge` | Clinical roles | Discharge patient |
| `PATCH` | `/transfer` | Clinical roles | Transfer patient |

### Laboratory — `/api/lab`

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/request` | ADMIN, DOCTOR | Create lab request |
| `POST` | `/request/search` | ADMIN, DOCTOR, LAB_TECH | Search lab requests |
| `GET` | `/{id}` | ADMIN, DOCTOR, LAB_TECH, NURSE | Lab request details |
| `PATCH` | `/{id}/status` | ADMIN, LAB_TECH | Update request status |
| `POST` | `/result` | ADMIN, LAB_TECH | Post lab results |
| `GET` | `/{id}/result` | ADMIN, LAB_TECH | Get results for request |

### Pharmacy — `/api/pharmacy`

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/add-medicine` | PHARMACIST, ADMIN | Add medicine |
| `POST` | `/search-medicine` | PHARMACIST, ADMIN | Search inventory |
| `GET` | `/medicine/{id}` | PHARMACIST, ADMIN | Medicine details |
| `PATCH` | `` | PHARMACIST, ADMIN | Update medicine |
| `PATCH` | `/{id}` | PHARMACIST, ADMIN | Add stock |
| `POST` | `/dispense` | PHARMACIST | Dispense medicine |
| `POST` | `/dispense/{id}` | PHARMACIST, ADMIN, DOCTOR | Dispense logs by prescription |
| `GET` | `/dispense-logs` | PHARMACIST, ADMIN, DOCTOR | All dispense logs |

### Billing — `/api/billing`

| Method | Path | Access | Description |
|---|---|---|---|
| `PATCH` | `/service-rate` | ADMIN | Update service rate |
| `GET` | `/service-rate` | ADMIN | Get all service rates |
| `POST` | `/generate/invoice` | ADMIN, CASHIER | Generate invoice |
| `POST` | `/search` | ADMIN, CASHIER | Search invoices |
| `GET` | `/{id}` | ADMIN, CASHIER, PATIENT | Invoice details |
| `POST` | `/{id}/items` | ADMIN, CASHIER, PATIENT | Add invoice items |
| `POST` | `/{id}/pay` | ADMIN, CASHIER | Record payment |
| `GET` | `/{id}/payments` | ADMIN, CASHIER | Payment list |
| `GET` | `/{id}/payment-detail` | ADMIN, CASHIER | Payment details |

### Records — `/api/records`

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/create` | ADMIN, DOCTOR | Create medical record |
| `GET` | `/{id}` | ADMIN, DOCTOR, NURSE | Get medical record |
| `PATCH` | `` | ADMIN, DOCTOR | Update medical record |
| `PATCH` | `/prescription` | ADMIN, DOCTOR | Add prescription |

### Vitals — `/api/vitals`

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `` | ADMIN, DOCTOR, NURSE | Log vitals |
| `GET` | `/{id}` | ADMIN, DOCTOR, NURSE, PATIENT | Patient vitals history |

### Reports — `/api/reports`

| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/dashboard` | ADMIN | Summary dashboard |
| `GET` | `/revenue` | ADMIN | Revenue report by date range |
| `GET` | `/beds/occupancy` | ADMIN | Bed occupancy per ward |

---

## 👥 Roles & Permissions

| Role | Description |
|---|---|
| `ADMIN` | Full system access |
| `DOCTOR` | Clinical operations, own patients |
| `NURSE` | Patient care, vitals, admissions (read) |
| `RECEPTIONIST` | Patient registration, appointments, admissions |
| `LAB_TECH` | Lab requests and results |
| `PHARMACIST` | Medicine inventory and dispensing |
| `CASHIER` | Billing and payments |
| `PATIENT` | Self-service: own records, appointments, invoices |

---

## 🗄 Data Models

### Core Relationships

```
User ──────────── Patient (1:1)
User ──────────── Doctor  (1:1)

Doctor ─────────── Department     (Many:1)
Doctor ─────────── DoctorSchedule (1:Many)

Patient ────────── Appointment  (1:Many)
Doctor  ────────── Appointment  (1:Many)

Patient ────────── Admission    (1:Many)
Bed     ────────── Admission    (1:Many)
Doctor  ────────── Admission    (1:Many)

Ward ───────────── Bed          (1:Many)

Patient ────────── MedicalRecord (1:Many)
Doctor  ────────── MedicalRecord (1:Many)
MedicalRecord ──── Prescription  (1:Many)
Prescription  ──── DispenseLog   (1:Many)

Patient ────────── LabRequest   (1:Many)
LabRequest  ─────── LabResult   (1:1)

Patient ────────── Invoice      (1:Many)
Invoice ────────── InvoiceItem  (1:Many)
Invoice ────────── Payment      (1:Many)

Patient ────────── Vitals       (1:Many)
```

### Enums Reference

| Enum | Values |
|---|---|
| `Role` | `ADMIN`, `DOCTOR`, `NURSE`, `RECEPTIONIST`, `LAB_TECH`, `PHARMACIST`, `CASHIER`, `PATIENT` |
| `AppointmentStatus` | `SCHEDULED`, `COMPLETED`, `CANCELLED`, `NO_SHOW` |
| `AppointmentType` | `CONSULTATION`, `FOLLOW_UP`, `EMERGENCY`, `WALK_IN` |
| `AdmissionStatus` | `ADMITTED`, `DISCHARGED`, `TRANSFERRED` |
| `BedStatus` | `AVAILABLE`, `OCCUPIED`, `MAINTENANCE` |
| `WardType` | `GENERAL`, `ICU`, `PEDIATRIC`, `MATERNITY`, `SURGICAL` |
| `LabPriority` | `ROUTINE`, `URGENT`, `STAT` |
| `LabRequestStatus` | `PENDING`, `IN_PROGRESS`, `COMPLETED` |
| `InvoiceStatus` | `UNPAID`, `PARTIAL`, `PAID` |
| `PaymentMethod` | `CASH`, `CARD`, `INSURANCE`, `GCASH` |
| `RateType` | `BED_GENERAL/ICU/PEDIATRIC/MATERNITY/SURGICAL`, `CONSULTATION`, `LAB_ROUTINE/URGENT/STAT` |
| `Gender` | `MALE`, `FEMALE`, `OTHER` |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.x

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/elysiae.git
cd elysiae
```

### 2. Create the Database

```sql
CREATE DATABASE elysiae_db;
```

### 3. Configure Application Properties

Create `src/main/resources/application.properties` (or `application.yml`):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/elysiae_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

jwt.secret=your-256-bit-secret-key-here-must-be-long-enough
jwt.expiration-ms=86400000

frontend.url=http://localhost:3000

management.endpoints.web.exposure.include=health
```

### 4. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

### 5. Seed Service Rates

Service rates must be seeded for billing to work. Insert records for each `RateType` value into the `service_rates` table:

```sql
INSERT INTO service_rates (service_key, rate, description, is_active) VALUES
('BED_GENERAL',    500.00,  'General ward daily rate',    true),
('BED_ICU',        2500.00, 'ICU daily rate',             true),
('BED_PEDIATRIC',  700.00,  'Pediatric ward daily rate',  true),
('BED_MATERNITY',  800.00,  'Maternity ward daily rate',  true),
('BED_SURGICAL',   1500.00, 'Surgical ward daily rate',   true),
('CONSULTATION',   300.00,  'Consultation fee',           true),
('LAB_ROUTINE',    150.00,  'Routine lab test',           true),
('LAB_URGENT',     300.00,  'Urgent lab test',            true),
('LAB_STAT',       500.00,  'STAT lab test',              true);
```

---

## ⚙️ Configuration

| Property | Description | Default |
|---|---|---|
| `jwt.secret` | HMAC-SHA256 signing key (min 32 chars) | — |
| `jwt.expiration-ms` | Token TTL in milliseconds | `86400000` (24h) |
| `frontend.url` | CORS allowed origin | `http://localhost:3000` |
| `spring.jpa.hibernate.ddl-auto` | Schema strategy (`update`/`create`) | `update` |

---

## 🧾 Credential Slip System

When a patient, doctor, or staff member is registered (or their credentials are reset), the system generates a **printable HTML credential slip** styled for an 80mm thermal receipt printer.

Three slip styles are available:

- **Patient Slip** — Soft green/teal theme with hospital branding
- **Doctor Slip** — Dark navy professional theme with department info
- **Staff Slip** — Minimal monochrome utilitarian style

Slips are returned as `text/html` responses and can be sent directly to a browser print dialog or receipt printer. They include the username, temporary password, date issued, and a reminder to change the password on first login.

> **Reprint endpoint:** `GET /api/receipt/{id}/slip?role=PATIENT|DOCTOR|<other>` — resets and reissues credentials.

---

## 🛡 Error Handling

All errors return a consistent `ErrorResponse` structure:

```json
{
  "now": "2024-01-15T10:30:00Z",
  "statusCode": 404,
  "message": "No patient found with that ID",
  "errors": null
}
```

Validation errors include a field-level `errors` map:

```json
{
  "now": "2024-01-15T10:30:00Z",
  "statusCode": 400,
  "message": "Validation failed",
  "errors": {
    "email": "Invalid email format",
    "phone": "Invalid phone number"
  }
}
```

---

## 📄 License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

<div align="center">
Built with ❤️ for healthcare operations
</div>
