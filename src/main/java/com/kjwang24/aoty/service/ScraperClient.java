package com.kjwang24.aoty.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;

import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ScraperClient {

    private final RestClient restClient = RestClient.create();

    public List<RecentlyPlayedItem> fetchRecentlyPlayed(String accessToken) {
        return restClient.get()
            .uri(uriBuilder -> uriBuilder
                .scheme("https").host("api.spotify.com").path("/v1/me/player/recently-played")
                .queryParam("limit", 50)
                .build()
            )
            .headers(headers -> headers.setBearerAuth(accessToken))
            .retrieve()
            .body(ScrapeResponse.class)
            .items();
    }

    private record ScrapeResponse(
        @JsonProperty("items") List<RecentlyPlayedItem> items) {}

    public record RecentlyPlayedItem(
        @JsonProperty("track") Track track,
        @JsonProperty("playedAt") Instant playedAt
    ) {}

    public record Track(
        @JsonProperty("id") String spotifyId,
        @JsonProperty("name") String songName,
        @JsonProperty("album") JsonNode album,
        @JsonProperty("artists") List<JsonNode> artists
    ) {}
}
