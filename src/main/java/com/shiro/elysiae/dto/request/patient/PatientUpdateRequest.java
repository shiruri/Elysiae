package com.shiro.elysiae.dto.request.patient;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientUpdateRequest (

    @Size(max = 100, message = "First name must not exceed {max} characters")
    String firstName,

    @Size(max = 100, message = "Last name must not exceed {max} characters")
    String lastName,

    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth,

    String gender,

    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Invalid blood type")
    String bloodType,

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    String phone,

    @Email(message = "Invalid email format")
    @Size(max = 120)
    String email,

    String address,
    String emergencyContactName,

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid emergency contact phone")
    String emergencyContactPhone
) {
}
