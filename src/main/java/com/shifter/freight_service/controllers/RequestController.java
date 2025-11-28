package com.shifter.freight_service.controllers;

import com.shifter.freight_service.clients.AuthServiceClient;
import com.shifter.freight_service.models.*;
import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import com.shifter.freight_service.services.EntityInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/request")
@CrossOrigin
public class RequestController {

    @Autowired
    private AuthServiceClient client;
    @Autowired
    private EntityInterface<Request> entityInterface;
    @Autowired
    private EntityInterface<Offer> offerInterface;

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


    @PatchMapping("/{id}/reject")
    public Object rejectRequest(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        Optional<Request> request = entityInterface.findEntityById(id, user);
        if  (request.isPresent()) {
            if (!request.get().getNotInterestUserIds().contains(user.getId())) {
                request.get().getNotInterestUserIds().add(user.getId());
            }
            return entityInterface.updateEntity(user, request.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Request with id " + id + " not found"));
        }
    }

    @PatchMapping("/{id}/accept")
    public Object acceptRequest(@RequestHeader("Authorization") String authHeader, @PathVariable Long id, @RequestBody List<Vehicle> vehicles) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        Optional<Request> request = entityInterface.findEntityById(id, user);
        if  (request.isPresent()) {
            Offer offer = Offer.builder()
                    .request(request.get())
                    .amount(request.get().getAmount())
                    .status(OfferStatus.ACCEPTED)
                    .createdBy(user.getId())
                    .vehicles(vehicles)
                    .build();
            offerInterface.addEntity(user, offer);
            request.get().setStatus(RequestStatus.ASSIGNED);
            return entityInterface.updateEntity(user, request.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Request with id " + id + " not found"));
        }
    }

    @PatchMapping("/{id}/negotiate")
    public Object negotiateRequest(@RequestHeader("Authorization") String authHeader, @PathVariable Long id, @RequestBody BigDecimal amount, @RequestBody List<Vehicle> vehicles) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        Optional<Request> request = entityInterface.findEntityById(id, user);
        if  (request.isPresent()) {
            Offer offer = Offer.builder()
                    .request(request.get())
                    .amount(amount)
                    .createdBy(user.getId())
                    .vehicles(vehicles)
                    .build();
            offer.setRequest(request.get());
            return offerInterface.addEntity(user, offer);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Request with id " + id + " not found"));
        }
    }


}
