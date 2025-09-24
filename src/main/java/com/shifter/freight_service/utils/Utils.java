package com.shifter.freight_service.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shifter.freight_service.payloads.responses.RoleResponse;
import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import com.shifter.freight_service.payloads.responses.PermissionResponse;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Component
@AllArgsConstructor
public class Utils {

    private final EntityManager entityManager;

    /**
     * Extract non-null properties of an entity object along with their values.
     */
    public Map<String, Object> getNonNullProperties(Class<?> entityClass, Object entityValues) {
        Map<String, Object> nonNullProperties = new HashMap<>();
        if (entityValues == null) return nonNullProperties;

        Class<?> current = entityClass;
        while (current != null && current != Object.class) {
            Field[] fields = current.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object value = field.get(entityValues);
                    if (value != null) {
                        // keep original behavior: skip id field
                        if ("id".equals(field.getName())) continue;

                        // numeric handling: include only if > 0 (keeps your old semantic)
                        if (Number.class.isAssignableFrom(field.getType()) || field.getType().isPrimitive()) {
                            try {
                                if (value instanceof Number) {
                                    if (((Number) value).doubleValue() > 0) {
                                        nonNullProperties.put(field.getName(), value);
                                    }
                                } else {
                                    // primitives like boolean/char — include them directly
                                    nonNullProperties.put(field.getName(), value);
                                }
                            } catch (Exception ex) {
                                nonNullProperties.put(field.getName(), value);
                            }
                        } else {
                            nonNullProperties.put(field.getName(), value);
                        }
                    }
                } catch (IllegalAccessException e) {
                    // log or print (use logger in prod)
                    System.out.println("Failed reading field " + field.getName() + ": " + e.getMessage());
                }
            }
            current = current.getSuperclass();
        }
        return nonNullProperties;
    }


    /**
     * Find all entities based on dynamically built JPQL query.
     */
    public <T> List<T> findAllByCustomQuery(Map<String, Object> queryParams, Class<T> entityClass) {
        String entityName = entityClass.getSimpleName();

        // if there are no filters, just select all
        if (queryParams == null || queryParams.isEmpty()) {
            String jpql = "SELECT e FROM " + entityName + " e";
            TypedQuery<T> query = entityManager.createQuery(jpql, entityClass);
            return query.getResultList();
        }

        StringBuilder jpql = new StringBuilder("SELECT e FROM " + entityName + " e WHERE ");

        // Dynamically build query conditions
        StringJoiner conditions = new StringJoiner(" AND ");
        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();

            if (fieldValue instanceof String) {
                conditions.add("LOWER(e." + fieldName + ") LIKE LOWER(CONCAT('%', :" + fieldName + ", '%'))");
            } else if (fieldValue != null && fieldValue.getClass().isAnnotationPresent(Entity.class)) {
                // Assume nested entity; query on its 'id' field
                conditions.add("e." + fieldName + ".id = :" + fieldName + "Id");
            } else {
                conditions.add("e." + fieldName + " = :" + fieldName);
            }
        }

        jpql.append(conditions);

        // Create a Query object
        TypedQuery<T> query = entityManager.createQuery(jpql.toString(), entityClass);

        // Set query parameters
        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();

            if (fieldValue instanceof String) {
                query.setParameter(fieldName, fieldValue);
            } else if (fieldValue != null && fieldValue.getClass().isAnnotationPresent(Entity.class)) {
                try {
                    Field idField = fieldValue.getClass().getDeclaredField("id");
                    idField.setAccessible(true);
                    Object idValue = idField.get(fieldValue);
                    query.setParameter(fieldName + "Id", idValue);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // keep going — log would be better in prod
                    System.out.println(e.getMessage());
                }
            } else {
                query.setParameter(fieldName, fieldValue);
            }
        }

        return query.getResultList();
    }



    public AuthUserResponse mapToAuthUserResponse(AuthUserResponse r) {
        AuthUserResponse user = new AuthUserResponse();
        user.setId(r.getId());
        user.setFirstname(r.getFirstname());
        user.setLastname(r.getLastname());
        user.setName(r.getName());
        if (r.getRole() != null) {
            RoleResponse role = new RoleResponse();
            role.setId(r.getRole().getId());
            role.setName(r.getRole().getName());
            user.setRole(role);
        }
        user.setPhone(r.getPhone());
        user.setPicture(r.getPicture());
        return user;
    }


    public String jsonExtractMessage(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(json);
            return jsonNode.path("message").asText("Unknown error occurred");
        } catch (Exception e) {
            return json;
        }
    }


    public Field findFieldInHierarchy(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {}
            current = current.getSuperclass();
        }
        return null;
    }

}
