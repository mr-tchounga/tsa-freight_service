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
@RequestMapping("/api/v1/orderLine")
@CrossOrigin
public class OrderLineController {

    @Autowired
    private AuthServiceClient client;
    @Autowired
    private EntityInterface<OrderLine> entityInterface;

    @GetMapping( {"{id}", ""} )
    public ResponseEntity<Object> getOrderLine(@RequestHeader("Authorization") String authHeader, @PathVariable(required = false) Long id) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        if (id == null) {
            return ResponseEntity.ok(entityInterface.findAllEntity(user));
        }
        return ResponseEntity.ok(entityInterface.findEntityById(id, user));
    }

    @PostMapping
    public ResponseEntity<Object> postOrderLine(@RequestHeader("Authorization") String authHeader, @RequestBody(required = false) OrderLine orderLine) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        if (orderLine != null) {
            if (orderLine.getId() != null) {
                return ResponseEntity.ok(entityInterface.findEntityById(orderLine.getId(), user));
            }
            return ResponseEntity.ok(entityInterface.findFilterAllEntity(user, orderLine));
        }
        return ResponseEntity.ok(entityInterface.findAllEntity(user));
    }

    @PostMapping("/new")
    public ResponseEntity<Object> addOrderLine(@RequestHeader("Authorization") String authHeader, @RequestBody OrderLine orderLine) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        return ResponseEntity.ok(entityInterface.addEntity(user, orderLine));
    }

    @PutMapping
    public ResponseEntity<Object> updateOrderLine(@RequestHeader("Authorization") String authHeader, @RequestBody OrderLine orderLine) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        return ResponseEntity.ok(entityInterface.updateEntity(user, orderLine));
    }

    @DeleteMapping("/{id}")
    public Object deleteOrderLine(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        AuthUserResponse user = client.getCurrentUser(authHeader);
        if  (!entityInterface.findEntityById(id, user).get().isVisible()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Resource Not Found",
                    "message", "No OrderLine found with id " + id
            ));
        }
        entityInterface.deleteEntity(user, id);
        return ResponseEntity.ok("Operation successful");
    }



}
