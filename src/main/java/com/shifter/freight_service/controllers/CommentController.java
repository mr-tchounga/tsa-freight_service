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
@RequestMapping("/api/v1/comment")
@CrossOrigin
public class CommentController {

    @Autowired
    private AuthServiceClient client;
    @Autowired
    private EntityInterface<Comment> entityInterface;

    @GetMapping( {"{id}", ""} )
    public ResponseEntity<Object> getComment(@RequestHeader("Authorization") String authHeader, @PathVariable(required = false) Long id) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        if (id == null) {
            return ResponseEntity.ok(entityInterface.findAllEntity(user));
        }
        return ResponseEntity.ok(entityInterface.findEntityById(id, user));
    }

    @PostMapping
    public ResponseEntity<Object> postComment(@RequestHeader("Authorization") String authHeader, @RequestBody(required = false) Comment comment) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        if (comment != null) {
            if (comment.getId() != null) {
                return ResponseEntity.ok(entityInterface.findEntityById(comment.getId(), user));
            }
            return ResponseEntity.ok(entityInterface.findFilterAllEntity(user, comment));
        }
        return ResponseEntity.ok(entityInterface.findAllEntity(user));
    }

    @PostMapping("/new")
    public ResponseEntity<Object> addComment(@RequestHeader("Authorization") String authHeader, @RequestBody Comment comment) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        return ResponseEntity.ok(entityInterface.addEntity(user, comment));
    }

    @PutMapping
    public ResponseEntity<Object> updateComment(@RequestHeader("Authorization") String authHeader, @RequestBody Comment comment) {
        AuthUserResponse user = client.getCurrentUser(authHeader);

        return ResponseEntity.ok(entityInterface.updateEntity(user, comment));
    }

    @DeleteMapping("/{id}")
    public Object deleteComment(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        AuthUserResponse user = client.getCurrentUser(authHeader);
        if  (!entityInterface.findEntityById(id, user).get().isVisible()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Resource Not Found",
                    "message", "No Comment found with id " + id
            ));
        }
        entityInterface.deleteEntity(user, id);
        return ResponseEntity.ok("Operation successful");
    }



}
