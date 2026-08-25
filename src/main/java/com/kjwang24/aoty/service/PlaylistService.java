package com.kjwang24.aoty.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kjwang24.aoty.entity.Entry;
import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.repository.EntryRepository;
import com.kjwang24.aoty.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final UserRepository userRepository;
    private final EntryRepository entryRepository;
    private final TokenRefreshService tokenRefreshService;
    private final PlaylistClient playlistClient;

    public void syncEntry(User user, Entry entry, Optional<String> existingSpotifyId) {
        String accessToken = tokenRefreshService.getValidAccessToken(user);
        if (user.getSpotifyPlaylistId() == null) {
            String playlistId = playlistClient.createPlaylist(accessToken);
            user.setSpotifyPlaylistId(playlistId);
            userRepository.save(user);
        }
        
        existingSpotifyId.ifPresent(spotifyId ->
            playlistClient.deleteTrack(accessToken, user.getSpotifyPlaylistId(), "spotify:track:" + spotifyId));
        
        int position = (int) entryRepository.countByUserAndDateLessThan(user, entry.getDate());
        playlistClient.addTrack(accessToken, user.getSpotifyPlaylistId(), "spotify:track:" + entry.getSpotifyId(), position);
    }

    public Optional<String> getPlaylistUrl(User user) {
        if (user.getSpotifyPlaylistId() == null) {
            return Optional.empty();
        }
        return Optional.of("https://open.spotify.com/playlist/" + user.getSpotifyPlaylistId());
    }

    public Optional<String> getPlaylistId(User user) {
        if (user.getSpotifyPlaylistId() == null) {
            return Optional.empty();
        }
        return Optional.of(user.getSpotifyPlaylistId());
    }

}
