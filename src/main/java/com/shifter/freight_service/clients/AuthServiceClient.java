package com.shifter.freight_service.clients;

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
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceClient {

    @Value("${auth.base-url:http://localhost:8086}")
    private String authBaseUrl;
    WebClient webClient = WebClient.builder().baseUrl(authBaseUrl).build();
    private final Utils utils;        // <--- make final so Lombok injects it


    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    /**
     * Accepts either full Authorization header ("Bearer <token>") or just raw token.
     * Forwards a Bearer header to auth service unchanged.
     */
    public AuthUserResponse getCurrentUser(String authorizationOrToken){
        return getCallAuthService(authorizationOrToken, "me");
    }

//    public AuthUserResponse getTransporterById(String authorizationOrToken, Long id){
//        Map<String, String> body = Map.of("id", id.toString());
//        return postCallAuthService(authorizationOrToken, "users/transporter", body);
//    }


    public AuthUserResponse getCallAuthService(String authorizationOrToken, String url){
        if (authorizationOrToken == null || authorizationOrToken.isBlank()){
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Missing or invalid Token in request");
        }

        // If caller passed full header, keep it. If they passed raw token, convert.
        String bearer = authorizationOrToken.startsWith("Bearer ") || authorizationOrToken.startsWith("bearer ")
                ? authorizationOrToken
                : "Bearer " + authorizationOrToken;

        try {
            AuthUserResponse resp = webClient.get()
                    .uri(authBaseUrl + "/api/v1/auth/" + url)
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

//    public AuthUserResponse postCallAuthService(String authorizationOrToken, String url, Map<String, String> body){
//        if (authorizationOrToken == null || authorizationOrToken.isBlank()){
//            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Missing or invalid Token in request");
//        }
//
//        // If caller passed full header, keep it. If they passed raw token, convert.
//        String bearer = authorizationOrToken.startsWith("Bearer ") || authorizationOrToken.startsWith("bearer ")
//                ? authorizationOrToken
//                : "Bearer " + authorizationOrToken;
//
//        try {
//            AuthUserResponse resp = webClient.post()
//                    .uri(authBaseUrl + "/api/v1/auth/" + url)
//                    .header(HttpHeaders.AUTHORIZATION, bearer)
//                    .accept(MediaType.APPLICATION_JSON)
//                    .retrieve()
//                    .body
//                    .bodyToMono(id)
//                    .timeout(REQUEST_TIMEOUT)
//                    .block();
//
//            if (resp == null){
//                throw new RemoteAuthException("Empty response from Auth service");
//            }
//            // safe mapping (utils is now injected)
//            return utils.mapToAuthUserResponse(resp);
//        } catch (WebClientResponseException.Forbidden | WebClientResponseException.Unauthorized e) {
//            throw new UnauthorizedException("Unauthorized when calling Auth service: " + e.getMessage(), e);
//        } catch (WebClientResponseException.NotFound e) {
//            throw new RemoteResourceNotFoundException("User not found in Auth service", e);
//        } catch (WebClientResponseException ex) {
//            throw new RemoteAuthException("Auth service error: " + utils.jsonExtractMessage(ex.getResponseBodyAsString()), ex);
//        } catch (Exception ex) {
//            throw new RemoteAuthException("Failed to call Auth service", ex);
//        }
//    }


}
