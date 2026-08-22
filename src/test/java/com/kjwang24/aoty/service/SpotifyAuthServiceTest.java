package com.kjwang24.aoty.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kjwang24.aoty.entity.SpotifyCredential;
import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.repository.SpotifyCredentialRepository;
import com.kjwang24.aoty.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class SpotifyAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpotifyCredentialRepository spotifyCredentialRepository;

    @InjectMocks
    private SpotifyAuthService spotifyAuthService;

    @Test
    void loginWithNewAccount_createsUserAndCredential() {
        when(userRepository.findByAccountId("kjwang24")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(spotifyCredentialRepository.findByUser(any(User.class))).thenReturn(Optional.empty());
        when(spotifyCredentialRepository.save(any(SpotifyCredential.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Instant expiration = Instant.now();
        spotifyAuthService.handleLogin("kjwang24", "katherine", "access", "refresh", expiration);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getAccountId()).isEqualTo("kjwang24");
        assertThat(userCaptor.getValue().getDisplayName()).isEqualTo("katherine");

        ArgumentCaptor<SpotifyCredential> credCaptor = ArgumentCaptor.forClass(SpotifyCredential.class);
        verify(spotifyCredentialRepository).save(credCaptor.capture());
        assertThat(credCaptor.getValue().getAccessToken()).isEqualTo("access");
        assertThat(credCaptor.getValue().getRefreshToken()).isEqualTo("refresh");
        assertThat(credCaptor.getValue().getExpiresAt()).isEqualTo(expiration);
    }

    @Test
    void loginWithExistingAccount_updatesExistingCredential() {
        User user = new User();
        user.setAccountId("kjwang24");
        user.setDisplayName("katherine");

        SpotifyCredential cred = new SpotifyCredential();
        cred.setUser(user);
        cred.setAccessToken("oldaccess");
        cred.setRefreshToken("oldrefresh");

        when(userRepository.findByAccountId("kjwang24")).thenReturn(Optional.of(user));
        when(spotifyCredentialRepository.findByUser(user)).thenReturn(Optional.of(cred));
        when(spotifyCredentialRepository.save(any(SpotifyCredential.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Instant expiration = Instant.now();
        spotifyAuthService.handleLogin("kjwang24", "katherine", "newaccess", "newrefresh", expiration);

        verify(userRepository, never()).save(any(User.class));

        ArgumentCaptor<SpotifyCredential> credCaptor = ArgumentCaptor.forClass(SpotifyCredential.class);
        verify(spotifyCredentialRepository).save(credCaptor.capture());
        assertThat(credCaptor.getValue().getAccessToken()).isEqualTo("newaccess");
        assertThat(credCaptor.getValue().getRefreshToken()).isEqualTo("newrefresh");
    }

}
