package com.shifter.freight_service.services;

import com.shifter.freight_service.models.Request;
import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import com.shifter.freight_service.repositories.RequestRepository;
import com.shifter.freight_service.utils.Utils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class RequestService implements EntityInterface<Request> {

    private final RequestRepository requestRepository;
    private Utils utils;

    @Override
    public Optional<Request> findEntityById(Long id, AuthUserResponse user) {
        Request entity = new Request();
        entity.setId(id);
        entity.setVisible(true);
        if (user.getRole().getName().equals("AFFRETEUR")) {
            entity.setCreatedBy(user.getId());
        }
        Map<String, Object> nonNullElements = utils.getNonNullProperties(Request.class, entity);
        nonNullElements.put("id", id);
        if (!user.getRole().getName().equals("ADMIN")) {
            nonNullElements.put("isVisible", true);
        }
        List<Request> entities = utils.findAllByCustomQuery(nonNullElements, Request.class);
        return entities.isEmpty() ? Optional.empty() : Optional.of(entities.getFirst());
    }

    @Override
    public List<Request> findAllEntity(AuthUserResponse user) {
        Request entity = new Request();
        entity.setVisible(true);
        if (user.getRole().getName().equals("AFFRETEUR")) {
            entity.setCreatedBy(user.getId());
        }
        Map<String, Object> nonNullElements = utils.getNonNullProperties(Request.class, entity);
        if (!user.getRole().getName().equals("ADMIN")) {
            nonNullElements.put("isVisible", true);
        }
        return utils.findAllByCustomQuery(nonNullElements, Request.class);
    }

    @Override
    public List<Request> findFilterAllEntity(AuthUserResponse user, Request entity) {
        if (user.getRole().getName().equals("AFFRETEUR")) {
            entity.setCreatedBy(user.getId());
        }
        Map<String, Object> nonNullElements = utils.getNonNullProperties(Request.class, entity);
        if (!user.getRole().getName().equals("ADMIN")) {
            nonNullElements.put("isVisible", true);
        }
        return utils.findAllByCustomQuery(nonNullElements, Request.class);
    }

    @Override
    public Request addEntity(AuthUserResponse user, Request entity) {
        if (!(user.getRole().getName().equals("ADMIN") || user.getRole().getName().equals("AFFRETEUR"))) {
            throw new RuntimeException("Only ADMIN and FREIGHTERS are allowed to perform this operation");
        }
        try {
            entity.setCreatedBy(user.getId());
            entity.setCreatedAt(Calendar.getInstance().getTime());
            return  requestRepository.save(entity);
        } catch (Exception e) {
            throw new RuntimeException("Error adding new request: " + e.getMessage());
        }
    }

    @Override
    public Request updateEntity(AuthUserResponse user, Request entity) {
        Optional<Request> previousEntity = requestRepository.findById(entity.getId());
        if (!(user.getRole().getName().equals("ADMIN") || user.getRole().getName().equals("AFFRETEUR"))) {
            throw new RuntimeException("Only ADMIN and FREIGHTERS are allowed to perform this operation");
        }

        if (previousEntity.isPresent()) {
            previousEntity.get().setUpdatedBy(user.getId());
            previousEntity.get().setUpdatedAt(Calendar.getInstance().getTime());
            Map<String, Object> nonNullElements = utils.getNonNullProperties(Request.class, entity);

            nonNullElements.forEach((key, value) -> {
                try {
                    Field field = utils.findFieldInHierarchy(Request.class, key);
                    // If not found, try converting 'isVisible' -> 'visible' (common mismatch)
                    if (field == null && key.startsWith("is") && key.length() > 2) {
                        String alt = Character.toLowerCase(key.charAt(2)) + key.substring(3);
                        field = utils.findFieldInHierarchy(Request.class, alt);
                    }
                    if (field == null) {
                        throw new NoSuchFieldException(key);
                    }
                    field.setAccessible(true);
                    field.set(previousEntity.get(), value);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException("Error updating field: '" + key + "':\n" + e.getMessage());
                }
            });
            return requestRepository.save(previousEntity.get());
        } else {
            throw new RuntimeException("Request with id '" + entity.getId() + "' not found");
        }
    }

    @Override
    public void deleteEntity(AuthUserResponse user, Long id) {
        try {
            if (!(user.getRole().getName().equals("ADMIN") || user.getRole().getName().equals("AFFRETEUR"))) {
                throw new RuntimeException("Only ADMIN and FREIGHTERS are allowed to perform this operation");
            }
            Request entity = new Request();
            entity.setId(id);
            entity.setVisible(false);
            updateEntity(user, entity);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting request with id '" + id + "': " + e.getMessage());
        }
    }


}
