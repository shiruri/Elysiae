package com.shiro.elysiae.dto.request.pharmacy;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public record MedicineSearchRequest(

        String name,
        String genericName,
        String category,
        Boolean lowStock,
        Boolean expiringSoon,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate expiryBefore

) {}