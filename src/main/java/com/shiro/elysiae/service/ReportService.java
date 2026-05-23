package com.shiro.elysiae.service;

import com.shiro.elysiae.dto.response.dashboard.BedOccupancyResponse;
import com.shiro.elysiae.dto.response.dashboard.DashboardResponse;
import com.shiro.elysiae.dto.response.dashboard.RevenueReportResponse;
import com.shiro.elysiae.model.enums.BedStatus;
import com.shiro.elysiae.model.enums.InvoiceStatus;
import com.shiro.elysiae.model.wardsbedsadmission.Bed;
import com.shiro.elysiae.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final BedRepository bedRepository;
    private final WardRepository wardRepository;



    public DashboardResponse getDashboard() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = startOfDay.plusDays(1);

        long totalPatients    = patientRepository.countActive();
        long totalDoctors     = doctorRepository.countActive();
        long appointmentsToday = appointmentRepository.countAppointmentsToday(startOfDay, endOfDay);
        BigDecimal revenueToday = invoiceRepository.revenueToday(startOfDay, endOfDay);
        BigDecimal totalRevenue = invoiceRepository.totalRevenueBetween(
                LocalDateTime.of(2000, 1, 1, 0, 0), LocalDateTime.now());
        long availableBeds  = bedRepository.countByStatus(BedStatus.AVAILABLE);
        long occupiedBeds   = bedRepository.countByStatus(BedStatus.OCCUPIED);
        long totalBeds      = bedRepository.countAllActive();

        return new DashboardResponse(
                totalPatients,
                totalDoctors,
                appointmentsToday,
                revenueToday,
                totalRevenue,
                availableBeds,
                occupiedBeds,
                totalBeds
        );
    }

    public RevenueReportResponse getRevenueReport(LocalDate from, LocalDate to) {
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt   = to.plusDays(1).atStartOfDay();

        BigDecimal totalPaid   = invoiceRepository.totalRevenueBetween(fromDt, toDt);
        BigDecimal totalBilled = invoiceRepository.totalBilledBetween(fromDt, toDt);
        BigDecimal totalUnpaid = totalBilled.subtract(totalPaid);

        long totalInvoices   = invoiceRepository.countByStatusBetween(fromDt, toDt, InvoiceStatus.UNPAID)
                + invoiceRepository.countByStatusBetween(fromDt, toDt, InvoiceStatus.PARTIAL)
                + invoiceRepository.countByStatusBetween(fromDt, toDt, InvoiceStatus.PAID);
        long paidInvoices    = invoiceRepository.countByStatusBetween(fromDt, toDt, InvoiceStatus.PAID);
        long partialInvoices = invoiceRepository.countByStatusBetween(fromDt, toDt, InvoiceStatus.PARTIAL);
        long unpaidInvoices  = invoiceRepository.countByStatusBetween(fromDt, toDt, InvoiceStatus.UNPAID);

        return new RevenueReportResponse(
                from, to,
                totalBilled,
                totalPaid,
                totalUnpaid,
                totalInvoices,
                paidInvoices,
                unpaidInvoices,
                partialInvoices
        );
    }

    public List<BedOccupancyResponse> getBedOccupancy() {
        return wardRepository.findAllActiveWithBeds().stream()
                .map(ward -> {
                    List<Bed> beds = ward.getBeds().stream()
                            .filter(b -> b.getDeletedAt() == null)
                            .toList();

                    long total       = beds.size();
                    long available   = beds.stream().filter(b -> b.getStatus() == BedStatus.AVAILABLE).count();
                    long occupied    = beds.stream().filter(b -> b.getStatus() == BedStatus.OCCUPIED).count();
                    long maintenance = beds.stream().filter(b -> b.getStatus() == BedStatus.MAINTENANCE).count();
                    double rate      = total > 0 ? (double) occupied / total * 100 : 0.0;

                    return new BedOccupancyResponse(
                            ward.getName(),
                            ward.getType(),
                            ward.getFloor(),
                            total,
                            available,
                            occupied,
                            maintenance,
                            Math.round(rate * 10.0) / 10.0  // round to 1 decimal
                    );
                })
                .toList();
    }
}
