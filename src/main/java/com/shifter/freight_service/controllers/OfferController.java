package com.shifter.freight_service.controllers;

import com.shifter.freight_service.clients.AuthServiceClient;
import com.shifter.freight_service.models.Offer;
import com.shifter.freight_service.models.OfferStatus;
import com.shifter.freight_service.models.Request;
import com.shifter.freight_service.models.RequestStatus;
import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import com.shifter.freight_service.services.EntityInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/offer")
@CrossOrigin
public class OfferController {

    @Autowired
    private AuthServiceClient client;
    @Autowired
    private EntityInterface<Offer> offerInterface;
    @Autowired
    private EntityInterface<Request> requestInterface;

    @GetMapping( {"{id}", ""} )
    public ResponseEntity<Object> getOffer(@RequestHeader("Authorization") String authHeader, @PathVariable(required = false) Long id) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        if (id == null) {
            return ResponseEntity.ok(offerInterface.findAllEntity(user));
        }
        return ResponseEntity.ok(offerInterface.findEntityById(id, user));
    }

    @PostMapping
    public ResponseEntity<Object> postOffer(@RequestHeader("Authorization") String authHeader, @RequestBody(required = false) Offer offer) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        if (offer != null) {
            if (offer.getId() != null) {
                return ResponseEntity.ok(offerInterface.findEntityById(offer.getId(), user));
            }
            return ResponseEntity.ok(offerInterface.findFilterAllEntity(user, offer));
        }
        return ResponseEntity.ok(offerInterface.findAllEntity(user));
    }

    @PostMapping("/new")
    public ResponseEntity<Object> addOffer(@RequestHeader("Authorization") String authHeader, @RequestBody Offer offer) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        return ResponseEntity.ok(offerInterface.addEntity(user, offer));
    }

    @PutMapping
    public ResponseEntity<Object> updateOffer(@RequestHeader("Authorization") String authHeader, @RequestBody Offer offer) {
        AuthUserResponse user = client.getCurrentUser(authHeader);
        if (offer != null) {
            if (offer.getId() != null) {
                Optional<Offer> entity = offerInterface.findEntityById(offer.getId(), user);
                if (entity.isPresent()) {
                    Request request = entity.get().getRequest();
                    RequestStatus status = request.getStatus();

                    if ((status == RequestStatus.OPENED || status == RequestStatus.UNDER_NEGOTIATION)) {
                        if (offer.getStatus().equals(OfferStatus.ACCEPTED)){
                            Object res = offerInterface.updateEntity(user, offer);
                            request.setStatus(RequestStatus.ASSIGNED);
                            requestInterface.updateEntity(user, request);
                            return ResponseEntity.ok(res);
                        }
                        if (offer.getStatus() == OfferStatus.REJECTED || offer.getStatus() == OfferStatus.CANCELLED){
                            Object res = offerInterface.updateEntity(user, offer);
                            request.setStatus(RequestStatus.REFUNDED);
                            requestInterface.updateEntity(user, request);
                            return ResponseEntity.ok(res);
                        }
                    } else {
                        if (!offer.getStatus().equals(OfferStatus.PENDING)) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                                    "message", "Only OPENED and UNDER_NEGOTIATION Offers can be ACCEPTED, REJECTED & CANCELLED"
                            ));
                        }

                    }

                }
            }
        }

        return ResponseEntity.ok(offerInterface.updateEntity(user, offer));
    }

    @DeleteMapping("/{id}")
    public Object deleteOffer(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        AuthUserResponse user = client.getCurrentUser(authHeader);
        if  (!offerInterface.findEntityById(id, user).get().isVisible()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Resource Not Found",
                    "message", "No Offer found with id " + id
            ));
        }
        offerInterface.deleteEntity(user, id);
        return ResponseEntity.ok("Operation successful");
    }

}
