package com.shifter.freight_service.services;

import com.shifter.freight_service.payloads.responses.AuthUserResponse;

import java.util.List;
import java.util.Optional;

public interface EntityInterface <T>{
    Optional<T> findEntityById(Long id, AuthUserResponse user);
    List<T> findAllEntity(AuthUserResponse user);
    List<T> findFilterAllEntity(AuthUserResponse user, T entity);
    T addEntity(AuthUserResponse user, T entity);
    T updateEntity(AuthUserResponse user, T entity);
    void deleteEntity(AuthUserResponse user, Long id);
}
