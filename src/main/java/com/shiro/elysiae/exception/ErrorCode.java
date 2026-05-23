package com.shiro.elysiae.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    SERVICE_RATE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Service Rate Not Found"
    ),
    SERVICE_RATE_INVALID(
            HttpStatus.BAD_REQUEST,
            "Invalid price or service type given"
    ),
    MEDICINE_INSUFFICIENT_STOCK(
            HttpStatus.BAD_REQUEST,
            "Medicine Stock insufficient"
    ),
    MEDICINE_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "Medicine Already exists"
    ),
    LICENSE_NUMBER_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "License Number Already exists"
    ),
    DEPARTMENT_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "Department Already exists"
    ),
    RECORD_SOURCE_REQUIRED(
            HttpStatus.NOT_FOUND,
            "No Record Resource found"
    ),

    PATIENT_NOT_YET_ADMITTED(
            HttpStatus.NOT_FOUND,
            "No Admission found with that ID"
    ),

    WARD_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "No Ward found with that ID"
    ),

    SCHEDULE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "No Schedule found with that ID"
    ),
    DEPARTMENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "No Department found with that ID"
    ),
    DOCTOR_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "No Doctor found with that ID"
    ),
    INVALID_GENDER(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Invalid Gender"
    ),
    EMPTY_UPDATE(
            HttpStatus.BAD_REQUEST,
            "No field to be updated"
    ),

    UNAUTHORIZED_ACCESS(
            HttpStatus.FORBIDDEN,
            "You do not have permission to access or modify this resource"
    ),
    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "No User found with that ID"
    ),

    INVALID_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "Invalid Request Please login again"
    ),

    PATIENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "No patient found with that ID"
    ),

    DUPLICATE_PATIENT(
            HttpStatus.CONFLICT,
            "Patient with same name/DOB/contact already exists"
    ),



    APPOINTMENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "No appointment found with that ID"
    ),

    TIME_SLOT_UNAVAILABLE(
            HttpStatus.CONFLICT,
            "Requested doctor slot is already booked"
    ),

    APPOINTMENT_CONFLICT(
            HttpStatus.CONFLICT,
            "Patient already has an appointment at that time"
    ),

    INVALID_APPOINTMENT_STATUS(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Illegal appointment status transition"
    ),

    INVALID_APPOINTMENT_STATUS_CHANGE(
            HttpStatus.UNAUTHORIZED,
            "Invalid change Patients are only allowed to cancel appointment"
    ),
    DOCTOR_NOT_AVAILABLE(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Doctor has no schedule on that date"
    ),

    BED_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "No bed found with that ID"
    ),

    BED_NOT_AVAILABLE(
            HttpStatus.CONFLICT,
            "Bed is already occupied"
    ),

    PATIENT_ALREADY_ADMITTED(
            HttpStatus.CONFLICT,
            "Patient is already admitted"
    ),

    ADMISSION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "No admission record found with that ID"
    ),

    INVALID_TRANSFER(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Transfer target bed is invalid or unavailable"
    ),



    MEDICAL_RECORD_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "No medical record found with that ID"
    ),

    UNAUTHORIZED_RECORD_ACCESS(
            HttpStatus.FORBIDDEN,
            "Unauthorized access to medical record"
    ),

    PRESCRIPTION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "No prescription found under that record"
    ),



    LAB_REQUEST_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "No lab request found with that ID"
    ),

    LAB_RESULT_ALREADY_SUBMITTED(
            HttpStatus.CONFLICT,
            "Results already submitted for this request"
    ),

    INVALID_LAB_STATUS_TRANSITION(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Invalid laboratory status transition"
    ),


    MEDICINE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "No medicine found with that ID"
    ),

    INSUFFICIENT_STOCK(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Insufficient medicine stock"
    ),

    PRESCRIPTION_ALREADY_DISPENSED(
            HttpStatus.CONFLICT,
            "Prescription already dispensed"
    ),

    EXPIRED_MEDICINE(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Medicine has already expired"
    ),



    INVOICE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "No invoice found with that ID"
    ),

    INVOICE_ALREADY_PAID(
            HttpStatus.CONFLICT,
            "Invoice is already paid"
    ),

    OVERPAYMENT(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Payment exceeds remaining balance"
    ),

    INVALID_PAYMENT_METHOD(
            HttpStatus.BAD_REQUEST,
            "Invalid payment method"
    ),


    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "You do not have permission to access this resource"
    ),

    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Requested resource was not found"
    ),

    DUPLICATE_RESOURCE(
            HttpStatus.CONFLICT,
            "Resource already exists"
    ),

    VALIDATION_ERROR(
            HttpStatus.BAD_REQUEST,
            "Validation failed"
    ),

    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "Invalid username or password"
    ),

    EXPIRED_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "Authentication token has expired"
    ),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred"
    );


    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatusCode() {
        return status.value();
    }
}