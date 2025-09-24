package com.shifter.freight_service.services;

import com.shifter.freight_service.models.Order;
import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import com.shifter.freight_service.repositories.OrderRepository;
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
public class OrderService implements EntityInterface<Order> {

    private final OrderRepository orderRepository;
    private Utils utils;

    @Override
    public Optional<Order> findEntityById(Long id, AuthUserResponse user) {
        Order entity = new Order();
        entity.setId(id);
        entity.setVisible(true);
        entity.setCreatedBy(user.getId());
        Map<String, Object> nonNullElements = utils.getNonNullProperties(Order.class, entity);
        nonNullElements.put("id", id);
        if (!user.getRole().getName().equals("ADMIN")) {
            nonNullElements.put("isVisible", true);
        }
        List<Order> entities = utils.findAllByCustomQuery(nonNullElements, Order.class);
        return entities.isEmpty() ? Optional.empty() : Optional.of(entities.getFirst());
    }

    @Override
    public List<Order> findAllEntity(AuthUserResponse user) {
        Order entity = new Order();
        entity.setVisible(true);
        entity.setCreatedBy(user.getId());
        Map<String, Object> nonNullElements = utils.getNonNullProperties(Order.class, entity);
        if (!user.getRole().getName().equals("ADMIN")) {
            nonNullElements.put("isVisible", true);
        }
        return utils.findAllByCustomQuery(nonNullElements, Order.class);
    }

    @Override
    public List<Order> findFilterAllEntity(AuthUserResponse user, Order entity) {
        entity.setCreatedBy(user.getId());
        Map<String, Object> nonNullElements = utils.getNonNullProperties(Order.class, entity);
        if (!user.getRole().getName().equals("ADMIN")) {
            nonNullElements.put("isVisible", true);
        }
        return utils.findAllByCustomQuery(nonNullElements, Order.class);
    }

    @Override
    public Order addEntity(AuthUserResponse user, Order entity) {
        try {
            entity.setCreatedBy(user.getId());
            entity.setCreatedAt(Calendar.getInstance().getTime());
            return  orderRepository.save(entity);
        } catch (Exception e) {
            throw new RuntimeException("Error adding new order: " + e.getMessage());
        }
    }

    @Override
    public Order updateEntity(AuthUserResponse user, Order entity) {
        Optional<Order> previousEntity = orderRepository.findById(entity.getId());

        if (previousEntity.isPresent()) {
            previousEntity.get().setUpdatedBy(user.getId());
            previousEntity.get().setUpdatedAt(Calendar.getInstance().getTime());
            Map<String, Object> nonNullElements = utils.getNonNullProperties(Order.class, entity);

            nonNullElements.forEach((key, value) -> {
                try {
                    Field field = utils.findFieldInHierarchy(Order.class, key);
                    // If not found, try converting 'isVisible' -> 'visible' (common mismatch)
                    if (field == null && key.startsWith("is") && key.length() > 2) {
                        String alt = Character.toLowerCase(key.charAt(2)) + key.substring(3);
                        field = utils.findFieldInHierarchy(Order.class, alt);
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
            return orderRepository.save(previousEntity.get());
        } else {
            throw new RuntimeException("Order with id '" + entity.getId() + "' not found");
        }
    }

    @Override
    public void deleteEntity(AuthUserResponse user, Long id) {
        try {
            Order entity = new Order();
            entity.setId(id);
            entity.setVisible(false);
            updateEntity(user, entity);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting order with id '" + id + "': " + e.getMessage());
        }
    }


}
