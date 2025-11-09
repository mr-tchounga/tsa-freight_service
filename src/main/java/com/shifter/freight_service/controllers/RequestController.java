package com.shifter.freight_service.controllers;

import com.shifter.freight_service.clients.AuthServiceClient;
import com.shifter.freight_service.models.Request;
import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import com.shifter.freight_service.services.EntityInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/request")
@CrossOrigin
public class RequestController {

    @Autowired
    private AuthServiceClient client;
    @Autowired
    private EntityInterface<Request> entityInterface;

    @GetMapping( {"{id}", ""} )
    public ResponseEntity<Object> getRequest(@RequestHeader("Authorization") String authHeader, @PathVariable(required = false) Long id) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        if (id == null) {
            return ResponseEntity.ok(entityInterface.findAllEntity(user));
        }
        return ResponseEntity.ok(entityInterface.findEntityById(id, user));
    }

    @PostMapping
    public ResponseEntity<Object> postRequest(@RequestHeader("Authorization") String authHeader, @RequestBody(required = false) Request request) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        if (request != null) {
            if (request.getId() != null) {
                return ResponseEntity.ok(entityInterface.findEntityById(request.getId(), user));
            }
            return ResponseEntity.ok(entityInterface.findFilterAllEntity(user, request));
        }
        return ResponseEntity.ok(entityInterface.findAllEntity(user));
    }

    @PostMapping("/new")
    public ResponseEntity<Object> addRequest(@RequestHeader("Authorization") String authHeader, @RequestBody Request request) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        return ResponseEntity.ok(entityInterface.addEntity(user, request));
    }

    @PutMapping
    public ResponseEntity<Object> updateRequest(@RequestHeader("Authorization") String authHeader, @RequestBody Request request) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        return ResponseEntity.ok(entityInterface.updateEntity(user, request));
    }

    @DeleteMapping("/{id}")
    public Object deleteRequest(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        AuthUserResponse user = client.getCurrentUser(authHeader);
        if  (!entityInterface.findEntityById(id, user).get().isVisible()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Resource Not Found",
                    "message", "No Request found with id " + id
            ));
        }
        entityInterface.deleteEntity(user, id);
        return ResponseEntity.ok("Operation successful");
    }

}
