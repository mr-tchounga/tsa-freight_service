package com.shifter.freight_service.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Position {
    private Double latitude;
    private Double longitude;
    private String address;
    private Instant time;
}
