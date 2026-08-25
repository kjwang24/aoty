package com.kjwang24.aoty.service;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.databind.JsonNode;

@Component
public class SearchClient {

    private final RestClient restClient = RestClient.create();

    public List<Track> search(String accessToken, String query) {
        return restClient.get()
            .uri(uriBuilder -> uriBuilder
                .scheme("https").host("api.spotify.com").path("/v1/search")
                .queryParam("q", "track:" + query)
                .queryParam("type", "track")
                .queryParam("limit", 5)
                .build()
            )
            .headers(headers -> headers.setBearerAuth(accessToken))
            .retrieve()
            .body(SearchResponse.class)
            .trackList().tracks();
    }

    private record SearchResponse(
        @JsonProperty("tracks") TrackList trackList) {}

    private record TrackList(
        @JsonProperty("items") List<Track> tracks
    ) {}

    public record Track(
        @JsonProperty("id") String spotifyId,
        @JsonProperty("name") String songName,
        @JsonProperty("album") JsonNode album,
        @JsonProperty("artists") List<JsonNode> artists
    ) {}
}
