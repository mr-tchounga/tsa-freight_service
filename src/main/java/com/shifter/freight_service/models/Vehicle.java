package com.shifter.freight_service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

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
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    @Column(name = "user_id")
    private Long transporterId;
    private String type;
    private String model;
    private String plateNumber;
    private Integer capacity;
    private Integer percentageCapacityUsed;
    private String note;
    private boolean isBusy;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="latitude", column=@Column(name="position_latitude")),
            @AttributeOverride(name="longitude", column=@Column(name="position_longitude")),
            @AttributeOverride(name="address", column=@Column(name="position_address")),
            @AttributeOverride(name="timestamp", column=@Column(name="position_timestamp"))
    })
    private Position currentPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", insertable = false, updatable = false)
    private Offer offer;
}
