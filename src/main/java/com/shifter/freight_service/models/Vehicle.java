package com.shifter.freight_service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Vehicle extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    private String type;
    private String model;
    private String plateNumber;
    private Integer capacity;
    private String note;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="latitude", column=@Column(name="position_latitude")),
            @AttributeOverride(name="longitude", column=@Column(name="position_longitude")),
            @AttributeOverride(name="address", column=@Column(name="position_address")),
            @AttributeOverride(name="timestamp", column=@Column(name="position_timestamp"))
    })
    private Position currentPosition;
}
