package com.shifter.freight_service.services;

import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import com.shifter.freight_service.repositories.CommentRepository;
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
public class CommentService implements EntityInterface<Comment> {

    private final CommentRepository commentRepository;
    private Utils utils;

    @Override
    public Optional<Comment> findEntityById(Long id, AuthUserResponse user) {
        Comment entity = new Comment();
        entity.setId(id);
        entity.setVisible(true);
        entity.setCreatedBy(user.getId());
        Map<String, Object> nonNullElements = utils.getNonNullProperties(Comment.class, entity);
        nonNullElements.put("id", id);
        if (!user.getRole().getName().equals("ADMIN")) {
            nonNullElements.put("isVisible", true);
        }
        List<Comment> entities = utils.findAllByCustomQuery(nonNullElements, Comment.class);
        return entities.isEmpty() ? Optional.empty() : Optional.of(entities.getFirst());
    }

    @Override
    public List<Comment> findAllEntity(AuthUserResponse user) {
        Comment entity = new Comment();
        entity.setVisible(true);
        entity.setCreatedBy(user.getId());
        Map<String, Object> nonNullElements = utils.getNonNullProperties(Comment.class, entity);
        if (!user.getRole().getName().equals("ADMIN")) {
            nonNullElements.put("isVisible", true);
        }
        return utils.findAllByCustomQuery(nonNullElements, Comment.class);
    }

    @Override
    public List<Comment> findFilterAllEntity(AuthUserResponse user, Comment entity) {
        entity.setCreatedBy(user.getId());
        Map<String, Object> nonNullElements = utils.getNonNullProperties(Comment.class, entity);
        if (!user.getRole().getName().equals("ADMIN")) {
            nonNullElements.put("isVisible", true);
        }
        return utils.findAllByCustomQuery(nonNullElements, Comment.class);
    }

    @Override
    public Comment addEntity(AuthUserResponse user, Comment entity) {
        try {
            entity.setCreatedBy(user.getId());
            entity.setCreatedAt(Calendar.getInstance().getTime());
            return  commentRepository.save(entity);
        } catch (Exception e) {
            throw new RuntimeException("Error adding new comment: " + e.getMessage());
        }
    }

    @Override
    public Comment updateEntity(AuthUserResponse user, Comment entity) {
        Optional<Comment> previousEntity = commentRepository.findById(entity.getId());

        if (previousEntity.isPresent()) {
            previousEntity.get().setUpdatedBy(user.getId());
            previousEntity.get().setUpdatedAt(Calendar.getInstance().getTime());
            Map<String, Object> nonNullElements = utils.getNonNullProperties(Comment.class, entity);

            nonNullElements.forEach((key, value) -> {
                try {
                    Field field = utils.findFieldInHierarchy(Comment.class, key);
                    // If not found, try converting 'isVisible' -> 'visible' (common mismatch)
                    if (field == null && key.startsWith("is") && key.length() > 2) {
                        String alt = Character.toLowerCase(key.charAt(2)) + key.substring(3);
                        field = utils.findFieldInHierarchy(Comment.class, alt);
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
            return commentRepository.save(previousEntity.get());
        } else {
            throw new RuntimeException("Comment with id '" + entity.getId() + "' not found");
        }
    }

    @Override
    public void deleteEntity(AuthUserResponse user, Long id) {
        try {
            Comment entity = new Comment();
            entity.setId(id);
            entity.setVisible(false);
            updateEntity(user, entity);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting comment with id '" + id + "': " + e.getMessage());
        }
    }


}
