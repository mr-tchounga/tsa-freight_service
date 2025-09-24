package com.shifter.freight_service.payloads.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PermissionResponse {
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    private RoleResponse role;
    String name;
}