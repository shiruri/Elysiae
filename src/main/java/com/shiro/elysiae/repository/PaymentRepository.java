package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.billing.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByInvoiceId(Long invoiceId, Pageable pageable);
    Payment findByInvoiceId(Long invoiceId);

}
