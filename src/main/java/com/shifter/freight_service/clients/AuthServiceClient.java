package com.shifter.freight_service.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shifter.freight_service.exceptions.RemoteAuthException;
import com.shifter.freight_service.exceptions.RemoteResourceNotFoundException;
import com.shifter.freight_service.exceptions.UnauthorizedException;
import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import com.shifter.freight_service.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthServiceClient {

    private final WebClient webClient;
    private final Utils utils;        // <--- make final so Lombok injects it

    @Value("${shop.auth.base-url:http://localhost:8086}")
    private String authBaseUrl;

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    /**
     * Accepts either full Authorization header ("Bearer <token>") or just raw token.
     * Forwards a Bearer header to auth service unchanged.
     */
    public AuthUserResponse getCurrentUser(String authorizationOrToken){
        if (authorizationOrToken == null || authorizationOrToken.isBlank()){
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Missing or invalid Token in request");
        }

        // If caller passed full header, keep it. If they passed raw token, convert.
        String bearer = authorizationOrToken.startsWith("Bearer ") || authorizationOrToken.startsWith("bearer ")
                ? authorizationOrToken
                : "Bearer " + authorizationOrToken;

        try {
            AuthUserResponse resp = webClient.get()
                    .uri(authBaseUrl + "/api/v1/auth/me")
                    .header(HttpHeaders.AUTHORIZATION, bearer)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(AuthUserResponse.class)
                    .timeout(REQUEST_TIMEOUT)
                    .block();

            if (resp == null){
                throw new RemoteAuthException("Empty response from Auth service");
            }
            // safe mapping (utils is now injected)
            return utils.mapToAuthUserResponse(resp);
        } catch (WebClientResponseException.Forbidden | WebClientResponseException.Unauthorized e) {
            throw new UnauthorizedException("Unauthorized when calling Auth service: " + e.getMessage(), e);
        } catch (WebClientResponseException.NotFound e) {
            throw new RemoteResourceNotFoundException("User not found in Auth service", e);
        } catch (WebClientResponseException ex) {
            throw new RemoteAuthException("Auth service error: " + utils.jsonExtractMessage(ex.getResponseBodyAsString()), ex);
        } catch (Exception ex) {
            throw new RemoteAuthException("Failed to call Auth service", ex);
        }
    }


}
