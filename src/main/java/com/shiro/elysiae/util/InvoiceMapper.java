package com.shiro.elysiae.util;

import com.shiro.elysiae.dto.response.billing.InvoiceDetails;
import com.shiro.elysiae.dto.response.billing.InvoiceItemSummary;
import com.shiro.elysiae.dto.response.billing.InvoiceSummary;
import com.shiro.elysiae.dto.response.billing.PaymentSummary;
import com.shiro.elysiae.model.billing.Invoice;
import com.shiro.elysiae.model.billing.InvoiceItem;
import com.shiro.elysiae.model.billing.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "patientId",       source = "patient.id")
    @Mapping(target = "patientFullName", expression = "java(invoice.getPatient().getFirstName() + \" \" + invoice.getPatient().getLastName())")
    @Mapping(target = "admissionId",     source = "admission.id")
    @Mapping(target = "remainingBalance", expression = "java(invoice.getTotalAmount().subtract(invoice.getPaidAmount()))")
    @Mapping(target = "createdAt",       expression = "java(invoice.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())")
    InvoiceSummary toSummary(Invoice invoice);

    @Mapping(target = "patientId",        source = "patient.id")
    @Mapping(target = "patientFullName",  expression = "java(invoice.getPatient().getFirstName() + \" \" + invoice.getPatient().getLastName())")
    @Mapping(target = "admissionId",      source = "admission.id")
    @Mapping(target = "remainingBalance", expression = "java(invoice.getTotalAmount().subtract(invoice.getPaidAmount()))")
    @Mapping(target = "createdAt",        expression = "java(invoice.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())")
    @Mapping(target = "items",            source = "items")
    @Mapping(target = "payments",         source = "payments")
    InvoiceDetails toDetails(Invoice invoice);

    InvoiceItemSummary toItemSummary(InvoiceItem item);

    PaymentSummary toPaymentSummary(Payment payment);

    List<InvoiceSummary> toSummaryList(List<Invoice> invoices);
}
