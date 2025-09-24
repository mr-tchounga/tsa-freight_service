package com.shifter.freight_service.controllers;

import com.shifter.freight_service.clients.AuthServiceClient;
import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import com.shifter.freight_service.services.EntityInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/category")
@CrossOrigin
public class CategoryController {

    @Autowired
    private AuthServiceClient client;
    @Autowired
    private EntityInterface<Category> entityInterface;

    @GetMapping( {"{id}", ""} )
    public ResponseEntity<Object> getCategory(@RequestHeader("Authorization") String authHeader, @PathVariable(required = false) Long id) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        if (id == null) {
            return ResponseEntity.ok(entityInterface.findAllEntity(user));
        }
        return ResponseEntity.ok(entityInterface.findEntityById(id, user));
    }

    @PostMapping
    public ResponseEntity<Object> postCategory(@RequestHeader("Authorization") String authHeader, @RequestBody(required = false) Category category) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        if (category != null) {
            if (category.getId() != null) {
                return ResponseEntity.ok(entityInterface.findEntityById(category.getId(), user));
            }
            return ResponseEntity.ok(entityInterface.findFilterAllEntity(user, category));
        }
        return ResponseEntity.ok(entityInterface.findAllEntity(user));
    }

    @PostMapping("/new")
    public ResponseEntity<Object> addCategory(@RequestHeader("Authorization") String authHeader, @RequestBody Category category) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        return ResponseEntity.ok(entityInterface.addEntity(user, category));
    }

    @PutMapping
    public ResponseEntity<Object> updateCategory(@RequestHeader("Authorization") String authHeader, @RequestBody Category category) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        return ResponseEntity.ok(entityInterface.updateEntity(user, category));
    }

    @DeleteMapping("/{id}")
    public Object deleteCategory(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        AuthUserResponse user = client.getCurrentUser(authHeader);
        if  (!entityInterface.findEntityById(id, user).get().isVisible()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Resource Not Found",
                    "message", "No Category found with id " + id
            ));
        }
        entityInterface.deleteEntity(user, id);
        return ResponseEntity.ok("Operation successful");
    }



}
