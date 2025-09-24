package com.shifter.freight_service.services;

import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import com.shifter.freight_service.repositories.OrderLineRepository;
import com.shifter.freight_service.utils.Utils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class OrderLineService implements EntityInterface<OrderLine> {

    private final OrderLineRepository orderLineRepository;
    private Utils utils;

    @Override
    public Optional<OrderLine> findEntityById(Long id, AuthUserResponse user) {
        OrderLine entity = new OrderLine();
        entity.setId(id);
        entity.setVisible(true);
        entity.setCreatedBy(user.getId());
        Map<String, Object> nonNullElements = utils.getNonNullProperties(OrderLine.class, entity);
        nonNullElements.put("id", id);
        if (!user.getRole().getName().equals("ADMIN")) {
            nonNullElements.put("isVisible", true);
        }
        List<OrderLine> entities = utils.findAllByCustomQuery(nonNullElements, OrderLine.class);
        return entities.isEmpty() ? Optional.empty() : Optional.of(entities.getFirst());
    }

    @Override
    public List<OrderLine> findAllEntity(AuthUserResponse user) {
        OrderLine entity = new OrderLine();
        entity.setVisible(true);
        entity.setCreatedBy(user.getId());
        Map<String, Object> nonNullElements = utils.getNonNullProperties(OrderLine.class, entity);
        if (!user.getRole().getName().equals("ADMIN")) {
            nonNullElements.put("isVisible", true);
        }
        return utils.findAllByCustomQuery(nonNullElements, OrderLine.class);
    }

    @Override
    public List<OrderLine> findFilterAllEntity(AuthUserResponse user, OrderLine entity) {
        entity.setCreatedBy(user.getId());
        Map<String, Object> nonNullElements = utils.getNonNullProperties(OrderLine.class, entity);
        if (!user.getRole().getName().equals("ADMIN")) {
            nonNullElements.put("isVisible", true);
        }
        return utils.findAllByCustomQuery(nonNullElements, OrderLine.class);
    }

    @Override
    public OrderLine addEntity(AuthUserResponse user, OrderLine entity) {
        try {
            entity.setCreatedBy(user.getId());
            entity.setCreatedAt(Calendar.getInstance().getTime());
            return  orderLineRepository.save(entity);
        } catch (Exception e) {
            throw new RuntimeException("Error adding new orderLine: " + e.getMessage());
        }
    }

    @Override
    public OrderLine updateEntity(AuthUserResponse user, OrderLine entity) {
        Optional<OrderLine> previousEntity = orderLineRepository.findById(entity.getId());

        if (previousEntity.isPresent()) {
            previousEntity.get().setUpdatedBy(user.getId());
            previousEntity.get().setUpdatedAt(Calendar.getInstance().getTime());
            Map<String, Object> nonNullElements = utils.getNonNullProperties(OrderLine.class, entity);

            nonNullElements.forEach((key, value) -> {
                try {
                    Field field = utils.findFieldInHierarchy(OrderLine.class, key);
                    // If not found, try converting 'isVisible' -> 'visible' (common mismatch)
                    if (field == null && key.startsWith("is") && key.length() > 2) {
                        String alt = Character.toLowerCase(key.charAt(2)) + key.substring(3);
                        field = utils.findFieldInHierarchy(OrderLine.class, alt);
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
            return orderLineRepository.save(previousEntity.get());
        } else {
            throw new RuntimeException("OrderLine with id '" + entity.getId() + "' not found");
        }
    }

    @Override
    public void deleteEntity(AuthUserResponse user, Long id) {
        try {
            OrderLine entity = new OrderLine();
            entity.setId(id);
            entity.setVisible(false);
            updateEntity(user, entity);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting orderLine with id '" + id + "': " + e.getMessage());
        }
    }


}
