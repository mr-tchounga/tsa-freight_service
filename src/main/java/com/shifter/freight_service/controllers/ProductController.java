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
@RequestMapping("/api/v1/product")
@CrossOrigin
public class ProductController {

    @Autowired
    private AuthServiceClient client;
    @Autowired
    private EntityInterface<Product> entityInterface;

    @GetMapping( {"{id}", ""} )
    public ResponseEntity<Object> getProduct(@RequestHeader("Authorization") String authHeader, @PathVariable(required = false) Long id) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        if (id == null) {
            return ResponseEntity.ok(entityInterface.findAllEntity(user));
        }
        return ResponseEntity.ok(entityInterface.findEntityById(id, user));
    }

    @PostMapping
    public ResponseEntity<Object> postProduct(@RequestHeader("Authorization") String authHeader, @RequestBody(required = false) Product product) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        if (product != null) {
            if (product.getId() != null) {
                return ResponseEntity.ok(entityInterface.findEntityById(product.getId(), user));
            }
            return ResponseEntity.ok(entityInterface.findFilterAllEntity(user, product));
        }
        return ResponseEntity.ok(entityInterface.findAllEntity(user));
    }

    @PostMapping("/new")
    public ResponseEntity<Object> addProduct(@RequestHeader("Authorization") String authHeader, @RequestBody Product product) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        return ResponseEntity.ok(entityInterface.addEntity(user, product));
    }

    @PutMapping
    public ResponseEntity<Object> updateProduct(@RequestHeader("Authorization") String authHeader, @RequestBody Product product) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        return ResponseEntity.ok(entityInterface.updateEntity(user, product));
    }

    @DeleteMapping("/{id}")
    public Object deleteProduct(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        AuthUserResponse user = client.getCurrentUser(authHeader);
        if  (!entityInterface.findEntityById(id, user).get().isVisible()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Resource Not Found",
                    "message", "No Product found with id " + id
            ));
        }
        entityInterface.deleteEntity(user, id);
        return ResponseEntity.ok("Operation successful");
    }



}
