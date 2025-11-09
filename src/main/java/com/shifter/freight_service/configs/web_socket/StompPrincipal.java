package com.shifter.freight_service.configs.web_socket;

import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import lombok.Getter;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Objects;

public class StompPrincipal implements Principal {

    private final String name;
    @Getter
    private final AuthUserResponse authUser;

    public StompPrincipal(AuthUserResponse authUser) {
        this.authUser = Objects.requireNonNull(authUser);
        this.name = String.valueOf(authUser.getId());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "StompPrincipal{" + "name='" + name + '\'' + ", role=" + authUser.getRole() + '}';
    }

}
