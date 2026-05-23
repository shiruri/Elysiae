package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.billing.ServiceRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceRateRepository extends JpaRepository<ServiceRate, Long> {

    Optional<ServiceRate> findByServiceKeyAndIsActiveTrue(String serviceKey);
}