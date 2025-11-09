package com.shifter.freight_service.services;

import com.shifter.freight_service.models.Offer;
import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import com.shifter.freight_service.repositories.OfferRepository;
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
public class OfferService implements EntityInterface<Offer> {

    private final OfferRepository offerRepository;
    private Utils utils;

    @Override
    public Optional<Offer> findEntityById(Long id, AuthUserResponse user) {
        Offer entity = new Offer();
        entity.setId(id);
        entity.setVisible(true);
        if (!user.getRole().getName().equals("ADMIN")) {
            entity.setCreatedBy(user.getId());
        }
        Map<String, Object> nonNullElements = utils.getNonNullProperties(Offer.class, entity);
        nonNullElements.put("id", id);
        if (!user.getRole().getName().equals("ADMIN")) {
            nonNullElements.put("isVisible", true);
        }
        List<Offer> entities = utils.findAllByCustomQuery(nonNullElements, Offer.class);
        return entities.isEmpty() ? Optional.empty() : Optional.of(entities.getFirst());
    }

    @Override
    public List<Offer> findAllEntity(AuthUserResponse user) {
        Offer entity = new Offer();
        entity.setVisible(true);
        if (!user.getRole().getName().equals("ADMIN")) {
            entity.setCreatedBy(user.getId());
        }
        Map<String, Object> nonNullElements = utils.getNonNullProperties(Offer.class, entity);
        if (!user.getRole().getName().equals("ADMIN")) {
            nonNullElements.put("isVisible", true);
        }
        return utils.findAllByCustomQuery(nonNullElements, Offer.class);
    }

    @Override
    public List<Offer> findFilterAllEntity(AuthUserResponse user, Offer entity) {
        if (!user.getRole().getName().equals("ADMIN")) {
            entity.setCreatedBy(user.getId());
        }
        Map<String, Object> nonNullElements = utils.getNonNullProperties(Offer.class, entity);
        if (!user.getRole().getName().equals("ADMIN")) {
            nonNullElements.put("isVisible", true);
        }
        return utils.findAllByCustomQuery(nonNullElements, Offer.class);
    }

    @Override
    public Offer addEntity(AuthUserResponse user, Offer entity) {
        try {
            entity.setCreatedBy(user.getId());
            entity.setCreatedAt(Calendar.getInstance().getTime());
            return  offerRepository.save(entity);
        } catch (Exception e) {
            throw new RuntimeException("Error adding new offer: " + e.getMessage());
        }
    }

    @Override
    public Offer updateEntity(AuthUserResponse user, Offer entity) {
        Optional<Offer> previousEntity = offerRepository.findById(entity.getId());

        if (previousEntity.isPresent()) {
            previousEntity.get().setUpdatedBy(user.getId());
            previousEntity.get().setUpdatedAt(Calendar.getInstance().getTime());
            Map<String, Object> nonNullElements = utils.getNonNullProperties(Offer.class, entity);

            nonNullElements.forEach((key, value) -> {
                try {
                    Field field = utils.findFieldInHierarchy(Offer.class, key);
                    // If not found, try converting 'isVisible' -> 'visible' (common mismatch)
                    if (field == null && key.startsWith("is") && key.length() > 2) {
                        String alt = Character.toLowerCase(key.charAt(2)) + key.substring(3);
                        field = utils.findFieldInHierarchy(Offer.class, alt);
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
            return offerRepository.save(previousEntity.get());
        } else {
            throw new RuntimeException("Offer with id '" + entity.getId() + "' not found");
        }
    }

    @Override
    public void deleteEntity(AuthUserResponse user, Long id) {
        try {
            Offer entity = new Offer();
            entity.setId(id);
            entity.setVisible(false);
            updateEntity(user, entity);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting offer with id '" + id + "': " + e.getMessage());
        }
    }


}
