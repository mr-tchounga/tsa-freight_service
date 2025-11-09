package com.shifter.freight_service.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Request extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
//    @Column(name = "transporter_id")
//    private Long transporterId;
    @Column(nullable = false)
    private BigDecimal amount;       // initial amount proposed by shipper
    private BigDecimal finalAmount;  // after bargain / agreed
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="latitude", column=@Column(name="start_latitude")),
            @AttributeOverride(name="longitude", column=@Column(name="start_longitude")),
            @AttributeOverride(name="address", column=@Column(name="start_address")),
            @AttributeOverride(name="timestamp", column=@Column(name="start_timestamp"))
    })
    private Position startPosition;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="latitude", column=@Column(name="end_latitude")),
            @AttributeOverride(name="longitude", column=@Column(name="end_longitude")),
            @AttributeOverride(name="address", column=@Column(name="end_address")),
            @AttributeOverride(name="timestamp", column=@Column(name="end_timestamp"))
    })
    private Position endPosition;
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoryPosition>  historyPositions;
    // additional metadata
    private String note;
    private Double weightKg;
    private Double volumeM3;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.OPENED;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ChatMessage> chatMessages;
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Files>  files;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Offer> offers;

    @Version
    private Long version; // optimistic locking to avoid double assignment

}
