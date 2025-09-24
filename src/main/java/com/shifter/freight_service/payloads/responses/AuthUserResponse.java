package com.shifter.freight_service.payloads.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthUserResponse {
    private Long id;
    private String firstname;
    private String lastname;
    private String name;
    private String email;
    private String phone;
    private String picture;
    private RoleResponse role;

//    @Data
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class RoleResponse {
//        private Long id;
//        private String name;
//    }
}
