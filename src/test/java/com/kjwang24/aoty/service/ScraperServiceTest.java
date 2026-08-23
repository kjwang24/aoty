package com.kjwang24.aoty.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.argThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.time.Instant;
import java.util.List;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.repository.ListeningRecordRepository;
import com.kjwang24.aoty.repository.UserRepository;
import com.kjwang24.aoty.service.EntryExceptions.DuplicateEntryException;
import com.kjwang24.aoty.service.SpotifyRecentlyPlayedClient.RecentlyPlayedItem;
import com.kjwang24.aoty.service.SpotifyRecentlyPlayedClient.Track;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
public class ScraperServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ListeningRecordRepository listeningRecordRepository;

    @Mock
    private TokenRefreshService tokenRefreshService;

    @Mock
    private SpotifyRecentlyPlayedClient spotifyRecentlyPlayedClient;

    @InjectMocks
    private ScraperService scraperService;

    @Test
    void scrapeNewItem_savesNewListeningRecord() {
        User user = new User();
        user.setAccountId("kjwang24");

        Instant instant = Instant.now();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode album = objectMapper.readTree("{\"images\":[{\"url\":\"http://cover\"}]}");
        JsonNode artist = objectMapper.readTree("{\"name\":\"parcels\"}");
        Track track = new Track("gamesofluck", "gamesofluck", album, List.of(artist));
        RecentlyPlayedItem item = new RecentlyPlayedItem(track, instant);

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(tokenRefreshService.getValidAccessToken(user)).thenReturn("access");
        when(spotifyRecentlyPlayedClient.fetchRecentlyPlayed("access")).thenReturn(List.of(item));
        when(listeningRecordRepository.existsByUserAndSpotifyIdAndPlayedAt(user, "gamesofluck", instant)).thenReturn(false);

        scraperService.scrape();

        verify(listeningRecordRepository).save(argThat(record ->
            record.getUser().equals(user)
                && record.getSpotifyId().equals("gamesofluck")
                && record.getPlayedAt().equals(instant)));
    }

    @Test
    void scrapeExistingItem_recognizesDuplicate() {
        User user = new User();
        user.setAccountId("kjwang24");

        Instant instant = Instant.now();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode album = objectMapper.readTree("{\"images\":[{\"url\":\"http://cover\"}]}");
        JsonNode artist = objectMapper.readTree("{\"name\":\"charli xcx\"}");
        Track track = new Track("everything is romantic", "everything is romantic", album, List.of(artist));
        RecentlyPlayedItem item = new RecentlyPlayedItem(track, instant);

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(tokenRefreshService.getValidAccessToken(user)).thenReturn("access");
        when(spotifyRecentlyPlayedClient.fetchRecentlyPlayed("access")).thenReturn(List.of(item));
        when(listeningRecordRepository.existsByUserAndSpotifyIdAndPlayedAt(user, "everything is romantic", instant)).thenReturn(true);

        scraperService.scrape();

        verify(listeningRecordRepository, never()).save(any());
    }

    @Test
    void invalidUserInAgenda_getsIgnored(CapturedOutput output) {
        User invalidUser = new User();
        invalidUser.setAccountId("invalidUser");
        invalidUser.setId(1L);
        User validUser = new User();
        validUser.setAccountId("validUser");
        validUser.setId(2L);

        Instant instant = Instant.now();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode album = objectMapper.readTree("{\"images\":[{\"url\":\"http://cover\"}]}");
        JsonNode artist = objectMapper.readTree("{\"name\":\"lorde\"}");
        Track track = new Track("sober", "sober", album, List.of(artist));
        RecentlyPlayedItem item = new RecentlyPlayedItem(track, instant);

        when(userRepository.findAll()).thenReturn(List.of(invalidUser, validUser));
        when(tokenRefreshService.getValidAccessToken(invalidUser)).thenReturn("no");
        when(tokenRefreshService.getValidAccessToken(validUser)).thenReturn("access");
        when(spotifyRecentlyPlayedClient.fetchRecentlyPlayed("no")).thenThrow(new IllegalStateException("invalid access token"));
        when(spotifyRecentlyPlayedClient.fetchRecentlyPlayed("access")).thenReturn(List.of(item));
        when(listeningRecordRepository.existsByUserAndSpotifyIdAndPlayedAt(validUser, "sober", instant)).thenReturn(false);

        scraperService.scrape();

        assertThat(output.getOut())
                .contains("warning");
        verify(listeningRecordRepository).save(argThat(record ->
            record.getUser().equals(validUser)
                && record.getSpotifyId().equals("sober")
                && record.getPlayedAt().equals(instant)));
    }
}
