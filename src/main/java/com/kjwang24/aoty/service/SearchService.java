package com.kjwang24.aoty.service;

import com.kjwang24.aoty.entity.User;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchClient searchClient;
    private final TokenRefreshService tokenRefreshService;

    public List<SearchResult> findTracks(User user, String query) {
        String accessToken = tokenRefreshService.getValidAccessToken(user);
        return searchClient.search(accessToken, query)
            .stream()
            .map(track -> new SearchResult(track.spotifyId(),
                track.songName(),
                track.artists().get(0).path("name").asString(),
                track.album().path("images").get(0).path("url").asString())
            )
            .collect(Collectors.toList());
    }

    public record SearchResult(
        String spotifyId, 
        String songName,
        String songArtist, 
        String songCoverArt
    ) {}
}
