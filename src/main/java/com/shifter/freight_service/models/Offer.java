package com.shifter.freight_service.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "offers", indexes = {
        @Index(name = "idx_offer_request", columnList = "request_id"),
        @Index(name = "idx_offer_carrier", columnList = "carrier_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Offer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", insertable = false, updatable = false)
    private Request request;

    private BigDecimal amount;
    private String message;

    @Enumerated(EnumType.STRING)
    private OfferStatus status; // e.g., PENDING, ACCEPTED, REJECTED
}
