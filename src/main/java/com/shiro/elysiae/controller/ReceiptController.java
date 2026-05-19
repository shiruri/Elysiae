package com.shiro.elysiae.controller;


import com.shiro.elysiae.model.enums.Role;
import com.shiro.elysiae.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/receipt")
public class ReceiptController {

    private final ReceiptService receiptService;


    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    @GetMapping("/{id}/slip")
    public ResponseEntity<String> reprintReceipt(@PathVariable long id,@RequestParam Role role) {
        String  slip = receiptService.reprintCredentialSlip(id,role);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(slip);
    }



}
