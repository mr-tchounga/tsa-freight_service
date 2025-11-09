package com.shifter.freight_service.controllers;

import com.shifter.freight_service.clients.AuthServiceClient;
import com.shifter.freight_service.models.Files;
import com.shifter.freight_service.models.Vehicle;
import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import com.shifter.freight_service.services.EntityInterface;
import com.shifter.freight_service.services.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vehicle")
@CrossOrigin
public class VehicleController {

    @Autowired
    private AuthServiceClient client;
    @Autowired
    private EntityInterface<Vehicle> entityInterface;

    @GetMapping( {"{id}", ""} )
    public ResponseEntity<Object> getVehicle(@RequestHeader("Authorization") String authHeader, @PathVariable(required = false) Long id) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        if (id == null) {
            return ResponseEntity.ok(entityInterface.findAllEntity(user));
        }
        return ResponseEntity.ok(entityInterface.findEntityById(id, user));
    }

    @PostMapping
    public ResponseEntity<Object> postVehicle(@RequestHeader("Authorization") String authHeader, @RequestBody(required = false) Vehicle vehicle) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        if (vehicle != null) {
            if (vehicle.getId() != null) {
                return ResponseEntity.ok(entityInterface.findEntityById(vehicle.getId(), user));
            }
            return ResponseEntity.ok(entityInterface.findFilterAllEntity(user, vehicle));
        }
        return ResponseEntity.ok(entityInterface.findAllEntity(user));
    }

    @PostMapping("/new")
    public ResponseEntity<Object> addVehicle(@RequestHeader("Authorization") String authHeader, @RequestBody Vehicle vehicle) {
        AuthUserResponse user = client.getCurrentUser(authHeader);
//        if (vehicle != null) {
//            if (vehicle.getOwnerId() != null) {
//                AuthUserResponse transporter = client.
//            }
//        }

        return ResponseEntity.ok(entityInterface.addEntity(user, vehicle));
    }

    @PutMapping
    public ResponseEntity<Object> updateVehicle(@RequestHeader("Authorization") String authHeader, @RequestBody Vehicle vehicle) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        return ResponseEntity.ok(entityInterface.updateEntity(user, vehicle));
    }

    @DeleteMapping("/{id}")
    public Object deleteVehicle(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        AuthUserResponse user = client.getCurrentUser(authHeader);
        if  (!entityInterface.findEntityById(id, user).get().isVisible()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Resource Not Found",
                    "message", "No Vehicle found with id " + id
            ));
        }
        entityInterface.deleteEntity(user, id);
        return ResponseEntity.ok("Operation successful");
    }

}
