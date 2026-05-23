package com.shiro.elysiae.service;

import com.shiro.elysiae.dto.request.billing.*;
import com.shiro.elysiae.dto.response.billing.*;
import com.shiro.elysiae.exception.AppException;
import com.shiro.elysiae.exception.ErrorCode;
import com.shiro.elysiae.model.User;
import com.shiro.elysiae.model.appointments.Appointment;
import com.shiro.elysiae.model.billing.Invoice;
import com.shiro.elysiae.model.billing.InvoiceItem;
import com.shiro.elysiae.model.billing.Payment;
import com.shiro.elysiae.model.billing.ServiceRate;
import com.shiro.elysiae.model.enums.AuditAction;
import com.shiro.elysiae.model.enums.InvoiceStatus;
import com.shiro.elysiae.model.enums.RateType;
import com.shiro.elysiae.model.enums.WardType;
import com.shiro.elysiae.model.laborotory.LabRequest;
import com.shiro.elysiae.model.patient.Patient;
import com.shiro.elysiae.model.pharmacy.DispenseLog;
import com.shiro.elysiae.model.pharmacy.Medicine;
import com.shiro.elysiae.model.wardsbedsadmission.Admission;
import com.shiro.elysiae.model.wardsbedsadmission.Bed;
import com.shiro.elysiae.model.wardsbedsadmission.Ward;
import com.shiro.elysiae.repository.*;
import com.shiro.elysiae.util.InvoiceMapper;
import com.shiro.elysiae.util.ServiceRateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BillingService {

    private final PatientRepository patientRepository;
    private final InvoiceRepository invoiceRepository;
    private final ServiceRateRepository serviceRateRepository;
    private final ServiceRateMapper serviceRateMapper;
    private final LabRequestRepository labRequestRepository;
    private final AdmissionRepository admissionRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceItemRepository invoiceItemRepository;
    private final MedicineRepository medicineRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public ServiceRateDetails updateServiceRate(ServiceRateUpdateRequest request) {
        if(request.type() == null) {
            throw new AppException(ErrorCode.SERVICE_RATE_INVALID);
        }
        if(request.rate() == null) {
            throw new AppException(ErrorCode.SERVICE_RATE_INVALID);
        }

        String serviceKey = request.type().name();
        ServiceRate serviceRate = serviceRateRepository.findByServiceKeyAndIsActiveTrue(serviceKey)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_RATE_NOT_FOUND));

        serviceRate.setRate(request.rate());
        if(request.description() != null) {
            serviceRate.setDescription(request.description());
        }

        serviceRate.setUpdatedAt(LocalDateTime.now());
        ServiceRate saved = serviceRateRepository.save(serviceRate);
        auditService.log(AuditAction.SERVICE_RATE_UPDATED.name(), saved.getServiceKey(), saved.getId());
        return serviceRateMapper.toDetails(saved);
    }    public List<ServiceRateDetails> getAllServiceRates() {
        return serviceRateRepository.findAll().stream().map(serviceRateMapper::toDetails).toList();
    }

    public Page<InvoiceSummary> getALlInvoice(InvoiceSearchRequest request, Pageable pageable) {
        return invoiceRepository.search(
                request.patientId(),
                request.status(),
                pageable
        ).map(invoiceMapper::toSummary);
    }

    public InvoiceDetails generateInvoice(InvoiceCreateRequest request) {

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new AppException(ErrorCode.PATIENT_NOT_FOUND));

        Admission admission = admissionRepository.findById(request.admissionId())
                .orElseThrow(() -> new AppException(ErrorCode.ADMISSION_NOT_FOUND));

        WardType wardType = admission.getBed().getWard().getType();

        LocalDateTime checkOut = admission.getDischargedAt() != null
                ? admission.getDischargedAt()
                : LocalDateTime.now();

        long daysStayed = Math.max(1, ChronoUnit.DAYS.between(
                admission.getAdmittedAt(),
                checkOut
        ));

        ServiceRate serviceRate = serviceRateRepository
                .findByServiceKeyAndIsActiveTrue("BED_" + wardType.name())
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_RATE_NOT_FOUND));

        BigDecimal totalWardFee = serviceRate.getRate()
                .multiply(BigDecimal.valueOf(daysStayed));

        Invoice invoice = Invoice.builder()
                .patient(patient)
                .admission(admission)
                .dueDate(request.dueDate())
                .totalAmount(totalWardFee)
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        auditService.log(AuditAction.INVOICE_GENERATED.name(),
                patient.getFirstName() + " " + patient.getLastName(), saved.getId());
        return invoiceMapper.toDetails(saved);
    }
    public InvoiceDetails getById(long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Invoice invoice = invoiceRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
            long currentUserId = Long.parseLong(auth.getName());
            if (invoice.getPatient().getUser().getId() != currentUserId) {
                throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
            }
        }

        return invoiceMapper.toDetails(invoice);
    }
    public InvoiceDetails recordPayment(long invoiceId, PaymentCreateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        long currentUserId = Long.parseLong(auth.getName());

        User cashier = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Invoice invoice = invoiceRepository.findByIdWithDetails(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        if (invoice.getStatus() == InvoiceStatus.PAID)
            throw new AppException(ErrorCode.INVOICE_ALREADY_PAID);

        BigDecimal remaining = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
        if (request.amount().compareTo(remaining) > 0)
            throw new AppException(ErrorCode.OVERPAYMENT);

        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(request.amount())
                .method(request.method())
                .receivedBy(cashier)
                .build();

        paymentRepository.save(payment);

        invoice.setPaidAmount(invoice.getPaidAmount().add(request.amount()));
        invoice.setStatus(
                invoice.getPaidAmount().compareTo(invoice.getTotalAmount()) >= 0
                        ? InvoiceStatus.PAID
                        : InvoiceStatus.PARTIAL
        );
        invoiceRepository.save(invoice);

        auditService.log(AuditAction.PAYMENT_RECEIVED.name(),
                invoice.getPatient().getFirstName() + " " + invoice.getPatient().getLastName(),
                invoiceId);

        return invoiceMapper.toDetails(
                invoiceRepository.findByIdWithDetails(invoiceId)
                        .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND))
        );
    }
    private BigDecimal resolveUnitPrice(InvoiceItemAddRequest req) {
        return switch (req.category()) {
            case MEDICINE -> {
                Medicine medicine = medicineRepository.findById(req.medicineId())
                        .orElseThrow(() -> new AppException(ErrorCode.MEDICINE_NOT_FOUND));
                yield medicine.getUnitPrice();
            }
            case LAB -> {
                LabRequest lab = labRequestRepository.findById(req.labRequestId())
                        .orElseThrow(() -> new AppException(ErrorCode.LAB_REQUEST_NOT_FOUND));
                yield serviceRateRepository
                        .findByServiceKeyAndIsActiveTrue("LAB_" + lab.getPriority().name())
                        .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND))
                        .getRate();
            }
            case CONSULTATION -> serviceRateRepository
                    .findByServiceKeyAndIsActiveTrue("CONSULTATION")
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND))
                    .getRate();
            case BED -> {
                Admission admission = admissionRepository.findById(req.admissionId())
                        .orElseThrow(() -> new AppException(ErrorCode.ADMISSION_NOT_FOUND));
                WardType wardType = admission.getBed().getWard().getType();
                yield serviceRateRepository
                        .findByServiceKeyAndIsActiveTrue("BED_" + wardType.name())
                        .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND))
                        .getRate();
            }
            case PROCEDURE -> {
                if (req.unitPrice() == null)
                    throw new AppException(ErrorCode.VALIDATION_ERROR);
                yield req.unitPrice();
            }
        };
    }

    public InvoiceDetails addItems(long invoiceId, List<InvoiceItemAddRequest> requests) {

        Invoice invoice = invoiceRepository.findByIdWithDetails(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        List<InvoiceItem> newItems = requests.stream()
                .map(req -> {
                    BigDecimal unitPrice = resolveUnitPrice(req);

                    return InvoiceItem.builder()
                            .invoice(invoice)
                            .description(req.description())
                            .category(req.category())
                            .unitPrice(unitPrice)
                            .quantity(req.quantity())
                            .subtotal(unitPrice.multiply(BigDecimal.valueOf(req.quantity())))
                            .build();
                })
                .toList();

        invoiceItemRepository.saveAll(newItems);
        invoice.getItems().addAll(newItems);

        BigDecimal newTotal = invoice.getItems().stream()
                .map(InvoiceItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.setTotalAmount(newTotal);
        invoiceRepository.save(invoice);

        auditService.log(AuditAction.INVOICE_ITEM_ADDED.name(),
                invoice.getPatient().getFirstName() + " " + invoice.getPatient().getLastName(),
                invoiceId);

        return invoiceMapper.toDetails(
                invoiceRepository.findByIdWithDetails(invoiceId)
                        .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND))
        );
    }

    public Page<PaymentSummary> getPaymentsFromInvoice(long id, Pageable pageable) {
        return paymentRepository.findByInvoiceId(id,pageable).map(invoiceMapper::toPaymentSummary);
    }

    public PaymentDetails getPaymentDetails(long id) {
        return invoiceMapper.toPaymentDetails(paymentRepository.findByInvoiceId(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND)));
    }

}
