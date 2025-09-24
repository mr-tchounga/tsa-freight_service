package com.shifter.freight_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long>, JpaSpecificationExecutor<OrderLine> {

    List<OrderLine> findAllByCreatedBy(Long user);
    Optional<OrderLine> findByIdAndCreatedBy(Long id, Long user);
}
