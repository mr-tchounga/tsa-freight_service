package com.shifter.freight_service.repositories;

import com.shifter.freight_service.models.Order;
import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    List<Order> findAllByCreatedBy(Long user);
    Optional<Order> findByIdAndCreatedBy(Long id, Long user);
}
