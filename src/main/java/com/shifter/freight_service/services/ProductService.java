package com.shifter.freight_service.services;

import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import com.shifter.freight_service.repositories.ProductRepository;
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
public class ProductService implements EntityInterface<Product> {

    private final ProductRepository productRepository;
    private Utils utils;

    @Override
    public Optional<Product> findEntityById(Long id, AuthUserResponse user) {
        Product entity = new Product();
        entity.setId(id);
        entity.setVisible(true);
        entity.setCreatedBy(user.getId());
        Map<String, Object> nonNullElements = utils.getNonNullProperties(Product.class, entity);
        nonNullElements.put("id", id);
        if (!user.getRole().getName().equals("ADMIN")) {
            nonNullElements.put("isVisible", true);
        }
        List<Product> entities = utils.findAllByCustomQuery(nonNullElements, Product.class);
        return entities.isEmpty() ? Optional.empty() : Optional.of(entities.getFirst());
    }

    @Override
    public List<Product> findAllEntity(AuthUserResponse user) {
        Product entity = new Product();
        entity.setVisible(true);
        entity.setCreatedBy(user.getId());
        Map<String, Object> nonNullElements = utils.getNonNullProperties(Product.class, entity);
        if (!user.getRole().getName().equals("ADMIN")) {
            nonNullElements.put("isVisible", true);
        }
        return utils.findAllByCustomQuery(nonNullElements, Product.class);
    }

    @Override
    public List<Product> findFilterAllEntity(AuthUserResponse user, Product entity) {
        entity.setCreatedBy(user.getId());
        Map<String, Object> nonNullElements = utils.getNonNullProperties(Product.class, entity);
        if (!user.getRole().getName().equals("ADMIN")) {
            nonNullElements.put("isVisible", true);
        }
        return utils.findAllByCustomQuery(nonNullElements, Product.class);
    }

    @Override
    public Product addEntity(AuthUserResponse user, Product entity) {
        try {
            entity.setCreatedBy(user.getId());
            entity.setCreatedAt(Calendar.getInstance().getTime());
            return  productRepository.save(entity);
        } catch (Exception e) {
            throw new RuntimeException("Error adding new product: " + e.getMessage());
        }
    }

    @Override
    public Product updateEntity(AuthUserResponse user, Product entity) {
        Optional<Product> previousEntity = productRepository.findById(entity.getId());

        if (previousEntity.isPresent()) {
            previousEntity.get().setUpdatedBy(user.getId());
            previousEntity.get().setUpdatedAt(Calendar.getInstance().getTime());
            Map<String, Object> nonNullElements = utils.getNonNullProperties(Product.class, entity);

            nonNullElements.forEach((key, value) -> {
                try {
                    Field field = utils.findFieldInHierarchy(Product.class, key);
                    // If not found, try converting 'isVisible' -> 'visible' (common mismatch)
                    if (field == null && key.startsWith("is") && key.length() > 2) {
                        String alt = Character.toLowerCase(key.charAt(2)) + key.substring(3);
                        field = utils.findFieldInHierarchy(Product.class, alt);
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
            return productRepository.save(previousEntity.get());
        } else {
            throw new RuntimeException("Product with id '" + entity.getId() + "' not found");
        }
    }

    @Override
    public void deleteEntity(AuthUserResponse user, Long id) {
        try {
            Product entity = new Product();
            entity.setId(id);
            entity.setVisible(false);
            updateEntity(user, entity);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting product with id '" + id + "': " + e.getMessage());
        }
    }


}
