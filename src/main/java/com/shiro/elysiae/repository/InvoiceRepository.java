package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.billing.Invoice;
import com.shiro.elysiae.model.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {


    @Query(
            value = """
                    SELECT i FROM Invoice i
                    JOIN FETCH i.patient p
                    LEFT JOIN FETCH i.admission a
                    WHERE i.patient.id = :patientId
                    ORDER BY i.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(i) FROM Invoice i
                    WHERE i.patient.id = :patientId
                    """
    )
    Page<Invoice> findByPatientId(
            @Param("patientId") Long patientId,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT i FROM Invoice i
                    JOIN FETCH i.patient p
                    LEFT JOIN FETCH i.items
                    LEFT JOIN FETCH i.payments
                    WHERE i.id = :id
                    """,
            countQuery = """
                    SELECT COUNT(i) FROM Invoice i
                    WHERE i.id = :id
                    """
    )
    Optional<Invoice> findByIdWithDetails(@Param("id") Long id);

    @Query(
            value = """
                    SELECT i FROM Invoice i
                    JOIN FETCH i.patient p
                    LEFT JOIN FETCH i.admission a
                    WHERE (:patientId IS NULL OR i.patient.id = :patientId)
                    AND   (:status    IS NULL OR i.status     = :status)
                    ORDER BY i.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(i) FROM Invoice i
                    WHERE (:patientId IS NULL OR i.patient.id = :patientId)
                    AND   (:status    IS NULL OR i.status     = :status)
                    """
    )
    Page<Invoice> search(
            @Param("patientId") Long patientId,
            @Param("status") InvoiceStatus status,
            Pageable pageable
    );
}
