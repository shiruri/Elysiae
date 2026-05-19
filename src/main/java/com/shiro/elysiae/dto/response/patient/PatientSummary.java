package com.shiro.elysiae.dto.response.patient;

import com.shiro.elysiae.model.enums.Gender;

public record PatientSummary(
        long id,

        String firstName,

        String lastName,

        Gender gender,

        String bloodType,

        int age
) {
}
