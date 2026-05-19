package com.shiro.elysiae.dto.request.patient;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record PatientSearchRequest (

    @Size(min = 2, max = 100, message = "Keyword must be between {min} and {max} characters")
    String keyword,

    String gender,

    String bloodType,

    @Min(value = 0, message = "Age from cannot be negative")
    @Max(value = 150, message = "Age from must be realistic")
    Integer ageFrom,

    @Min(value = 0, message = "Age to cannot be negative")
    @Max(value = 150, message = "Age to must be realistic")
    Integer ageTo

    ) {
}
