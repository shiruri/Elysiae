package com.shiro.elysiae.controller;

import com.shiro.elysiae.dto.request.billing.*;
import com.shiro.elysiae.dto.response.billing.*;
import com.shiro.elysiae.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingService billingService;

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/service-rate")
    public ResponseEntity<ServiceRateDetails> updateServiceRate(@RequestBody ServiceRateUpdateRequest request) {
        return ResponseEntity.ok().body(billingService.updateServiceRate(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/service-rate")
    public ResponseEntity<List<ServiceRateDetails>> getAllServiceRates() {
        return ResponseEntity.ok().body(billingService.getAllServiceRates());
    }

    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @PostMapping("/generate/invoice")
    public ResponseEntity<InvoiceDetails> generateInvoice(@Valid @RequestBody InvoiceCreateRequest request) {
        return ResponseEntity.ok().body(billingService.generateInvoice(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @PostMapping("/search")
    public ResponseEntity<Page<InvoiceSummary>> getALlInvoice(@RequestBody InvoiceSearchRequest request, Pageable pageable) {
        return ResponseEntity.ok().body(billingService.getALlInvoice(request,pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','PATIENT')")
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDetails> getInvoiceById(@PathVariable long id) {
        return ResponseEntity.ok().body(billingService.getById(id));
    }
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','PATIENT')")
    @PostMapping("/{id}/items")
    public ResponseEntity<InvoiceDetails> addInvoiceItem(@PathVariable long id,
                                                         @Valid @RequestBody
                                                         List<InvoiceItemAddRequest> request) {
        return ResponseEntity.ok().body(billingService.addItems(id,request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @PostMapping("/{id}/pay")
    public ResponseEntity<InvoiceDetails> addPayment(@PathVariable long id,
                                                         @Valid @RequestBody
                                                         PaymentCreateRequest request) {
        return ResponseEntity.ok().body(billingService.recordPayment(id,request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @GetMapping("/{id}/payments")
    public ResponseEntity<Page<PaymentSummary>> getPaymentsFromInvoice(@PathVariable long id, Pageable pageable) {
        return ResponseEntity.ok().body(billingService.getPaymentsFromInvoice(id,pageable));
    }
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @GetMapping("/{id}/payment-detail")
    public ResponseEntity<PaymentDetails> getPaymentDetails(@PathVariable long id) {
        return ResponseEntity.ok().body(billingService.getPaymentDetails(id));
    }
}
