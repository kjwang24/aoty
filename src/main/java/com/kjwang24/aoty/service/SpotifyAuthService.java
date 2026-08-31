package com.kjwang24.aoty.service;

import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.entity.SpotifyCredential;
import com.kjwang24.aoty.repository.SpotifyCredentialRepository;
import com.kjwang24.aoty.repository.UserRepository;

import java.time.Instant;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpotifyAuthService {
    
    private final UserRepository userRepository;
    private final SpotifyCredentialRepository spotifyCredentialRepository;

    public void handleLogin(String accountId, String displayName, String pfpUrl, String accessToken, String refreshToken, Instant expiresAt) {
        User user = userRepository.findByAccountId(accountId).orElseGet(() -> {
            User newUser = new User();
            newUser.setAccountId(accountId);
            newUser.setDisplayName(displayName);
            return userRepository.save(newUser);
        });
        user.setPfpUrl(pfpUrl);
        userRepository.save(user);

        SpotifyCredential cred = spotifyCredentialRepository.findByUser(user).orElseGet(() -> {
            SpotifyCredential newCred = new SpotifyCredential();
            newCred.setUser(user);
            return newCred;
        });
        cred.setAccessToken(accessToken);
        cred.setRefreshToken(refreshToken);
        cred.setExpiresAt(expiresAt);
        spotifyCredentialRepository.save(cred);
    }
}
