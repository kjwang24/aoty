package com.kjwang24.aoty.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjwang24.aoty.entity.SpotifyCredential;
import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.repository.SpotifyCredentialRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenRefreshService {

    private final SpotifyCredentialRepository spotifyCredentialRepository;
    private final RestClient restClient = RestClient.create();

    @Value("${spring.security.oauth2.client.registration.spotify.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.spotify.client-secret}")
    private String clientSecret;

    public String getValidAccessToken(User user) {
        SpotifyCredential cred = spotifyCredentialRepository.findByUser(user).orElseThrow(() -> new IllegalStateException("no creds exist for user " + user.getId()));

        if (cred.getExpiresAt().isAfter(Instant.now())) {
            return cred.getAccessToken();
        }

        return refresh(cred).getAccessToken();
    }

    private SpotifyCredential refresh(SpotifyCredential cred) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", cred.getRefreshToken());

        TokenResponse response = restClient.post()
                .uri("https://accounts.spotify.com/api/token")
                .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(TokenResponse.class);

        cred.setAccessToken(response.accessToken());
        cred.setExpiresAt(Instant.now().plusSeconds(response.expiresIn()));
        if (response.refreshToken() != null) {
            cred.setRefreshToken(response.refreshToken());
        }
        return spotifyCredentialRepository.save(cred);
    }

    private record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") long expiresIn,
            @JsonProperty("refresh_token") String refreshToken) {
    }
}
