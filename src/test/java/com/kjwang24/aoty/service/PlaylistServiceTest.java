package com.kjwang24.aoty.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kjwang24.aoty.entity.Entry;
import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.repository.EntryRepository;
import com.kjwang24.aoty.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class PlaylistServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntryRepository entryRepository;

    @Mock
    private PlaylistClient playlistClient;

    @Mock
    private TokenRefreshService tokenRefreshService;

    @InjectMocks
    private PlaylistService playlistService;

    @Test
    void onlyInitialSync_createsPlaylist() {
        User user = new User();
        user.setAccountId("kjwang24");

        LocalDate date = LocalDate.now();
        Entry entry = new Entry();
        entry.setSpotifyId("halloween");
        entry.setDate(date);

        when(tokenRefreshService.getValidAccessToken(user)).thenReturn("access");
        when(playlistClient.createPlaylist("access")).thenReturn("playlist");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(entryRepository.countByUserAndDateLessThan(user, date)).thenReturn(0L);

        playlistService.syncEntry(user, entry, Optional.empty());

        assertThat(user.getSpotifyPlaylistId()).isEqualTo("playlist");
        verify(playlistClient).addTrack("access", "playlist", "spotify:track:halloween", 0);

        playlistService.syncEntry(user, entry, Optional.empty());
        verify(playlistClient, times(1)).createPlaylist("access");
    }

    @Test
    void syncNewTrackToExistingPlaylist_addsTrackOnly() {
        User user = new User();
        user.setAccountId("kjwang24");
        user.setSpotifyPlaylistId("playlist");

        LocalDate date = LocalDate.now();
        Entry entry = new Entry();
        entry.setSpotifyId("punisher");
        entry.setDate(date);

        when(tokenRefreshService.getValidAccessToken(user)).thenReturn("access");
        when(entryRepository.countByUserAndDateLessThan(user, date)).thenReturn(0L);

        playlistService.syncEntry(user, entry, Optional.empty());

        verify(playlistClient, never()).createPlaylist(anyString());
        verify(playlistClient).addTrack("access", "playlist", "spotify:track:punisher", 0);
    }

    @Test
    void replaceTrackonExistingPlaylist_deletesAndAddsTrack() {
        User user = new User();
        user.setAccountId("kjwang24");
        user.setSpotifyPlaylistId("playlist");

        LocalDate date = LocalDate.now();
        Entry entry = new Entry();
        entry.setSpotifyId("sunset");
        entry.setDate(date);

        when(tokenRefreshService.getValidAccessToken(user)).thenReturn("access");
        when(entryRepository.countByUserAndDateLessThan(user, date)).thenReturn(0L);

        playlistService.syncEntry(user, entry, Optional.of("hotel california"));

        verify(playlistClient).deleteTrack("access", "playlist", "spotify:track:hotel california");
        verify(playlistClient).addTrack("access", "playlist", "spotify:track:sunset", 0);
    }
}
