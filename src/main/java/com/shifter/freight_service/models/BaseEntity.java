package com.shifter.freight_service.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class BaseEntity {
    @CreatedBy
    protected Long createdBy;

    @JsonFormat(pattern = "dd/MM/yyyy HH:m:ss", timezone = "GMT+1")
    @CreatedDate
    protected Date createdAt;

    @LastModifiedBy
    protected Long updatedBy;

    @JsonFormat(pattern = "dd/MM/yyyy HH:m:ss", timezone = "GMT+1")
    @LastModifiedDate
    protected Date updatedAt;

    @Column
    @Builder.Default
    @JsonIgnore
    protected boolean isVisible = true;
}