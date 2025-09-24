package com.shifter.freight_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shifter.freight_service.models.Files;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<Files, Long>, JpaSpecificationExecutor<Files> {

    List<Files> findAllByUserId(Long userId);
    Optional<Files> findByIdAndUserId(Long id, Long userId);
}
