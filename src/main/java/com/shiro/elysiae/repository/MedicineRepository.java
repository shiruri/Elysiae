package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.pharmacy.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine,Long> {

    @Query(
            value = """
        SELECT m FROM Medicine m
        WHERE (:name         IS NULL OR LOWER(m.name)        LIKE LOWER(CONCAT('%', :name, '%')))
        AND   (:genericName  IS NULL OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :genericName, '%')))
        AND   (:category     IS NULL OR LOWER(m.category)    LIKE LOWER(CONCAT('%', :category, '%')))
        AND   (:lowStock     IS NULL OR (:lowStock = true AND m.stockQuantity <= m.reorderLevel))
        AND   (:expiryBefore IS NULL OR m.expiryDate <= :expiryBefore)
        AND   m.deletedAt IS NULL
        ORDER BY m.name ASC
        """,
            countQuery = """
        SELECT COUNT(m) FROM Medicine m
        WHERE (:name         IS NULL OR LOWER(m.name)        LIKE LOWER(CONCAT('%', :name, '%')))
        AND   (:genericName  IS NULL OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :genericName, '%')))
        AND   (:category     IS NULL OR LOWER(m.category)    LIKE LOWER(CONCAT('%', :category, '%')))
        AND   (:lowStock     IS NULL OR (:lowStock = true AND m.stockQuantity <= m.reorderLevel))
        AND   (:expiryBefore IS NULL OR m.expiryDate <= :expiryBefore)
        AND   m.deletedAt IS NULL
        """
    )
    Page<Medicine> search(
            @Param("name")         String name,
            @Param("genericName")  String genericName,
            @Param("category")     String category,
            @Param("lowStock")     Boolean lowStock,
            @Param("expiryBefore") LocalDate expiryBefore,
            Pageable pageable
    );

    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(String name);
}
