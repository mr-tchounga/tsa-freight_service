package com.shifter.freight_service.controllers;

import com.shifter.freight_service.clients.AuthServiceClient;
import com.shifter.freight_service.models.Order;
import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import com.shifter.freight_service.services.EntityInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/order")
@CrossOrigin
public class OrderController {

    @Autowired
    private AuthServiceClient client;
    @Autowired
    private EntityInterface<Order> entityInterface;

    @GetMapping( {"{id}", ""} )
    public ResponseEntity<Object> getOrder(@RequestHeader("Authorization") String authHeader, @PathVariable(required = false) Long id) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        if (id == null) {
            return ResponseEntity.ok(entityInterface.findAllEntity(user));
        }
        return ResponseEntity.ok(entityInterface.findEntityById(id, user));
    }

    @PostMapping
    public ResponseEntity<Object> postOrder(@RequestHeader("Authorization") String authHeader, @RequestBody(required = false) Order order) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        if (order != null) {
            if (order.getId() != null) {
                return ResponseEntity.ok(entityInterface.findEntityById(order.getId(), user));
            }
            return ResponseEntity.ok(entityInterface.findFilterAllEntity(user, order));
        }
        return ResponseEntity.ok(entityInterface.findAllEntity(user));
    }

    @PostMapping("/new")
    public ResponseEntity<Object> addOrder(@RequestHeader("Authorization") String authHeader, @RequestBody Order order) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        return ResponseEntity.ok(entityInterface.addEntity(user, order));
    }

    @PutMapping
    public ResponseEntity<Object> updateOrder(@RequestHeader("Authorization") String authHeader, @RequestBody Order order) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        return ResponseEntity.ok(entityInterface.updateEntity(user, order));
    }

    @DeleteMapping("/{id}")
    public Object deleteOrder(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        AuthUserResponse user = client.getCurrentUser(authHeader);
        if  (!entityInterface.findEntityById(id, user).get().isVisible()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Resource Not Found",
                    "message", "No Order found with id " + id
            ));
        }
        entityInterface.deleteEntity(user, id);
        return ResponseEntity.ok("Operation successful");
    }



}
