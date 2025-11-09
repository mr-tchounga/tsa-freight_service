package com.shifter.freight_service.repositories;

import com.shifter.freight_service.models.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {

    /**
     * Returns all vehicles whose createdBy equals :userId.
     * We use explicit JPQL because the property name in the DB/entity is 'createdBy'
     * (inherited from BaseEntity) but the public method name must remain findAllByUserId.
     */
    @Query("SELECT r FROM Vehicle r WHERE r.createdBy = :userId")
    List<Vehicle> findAllByUserId(@Param("userId") Long userId);

    /**
     * Returns the vehicle with given id only if it was created by the userId.
     */
    @Query("SELECT r FROM Vehicle r WHERE r.id = :id AND r.createdBy = :userId")
    Optional<Vehicle> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
