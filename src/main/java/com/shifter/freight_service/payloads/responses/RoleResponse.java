package com.shifter.freight_service.payloads.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleResponse {
    private Long id;
    private String name;
}
