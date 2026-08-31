package com.kjwang24.aoty.service;

import java.util.Base64;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class PlaylistClient {

    private final RestClient restClient = RestClient.create();

    public String createPlaylist(String accessToken) {
        String playlistId = restClient.post()
            .uri(uriBuilder -> uriBuilder
                .scheme("https").host("api.spotify.com").path("/v1/me/playlists")
                .build()
            )
            .headers(headers -> headers.setBearerAuth(accessToken))
            .body(new CreatePlaylistRequest("year in music", false, ""))
            .retrieve()
            .body(CreatePlaylistResponse.class)
            .id();
        setPublic(accessToken, playlistId, false); // spotify is not strict w this so need to enforce post-creation
        return playlistId;
    }

    public void setPublic(String accessToken, String playlistId, boolean isPublic) {
        restClient.put()
            .uri(uriBuilder -> uriBuilder
                .scheme("https").host("api.spotify.com").path("/v1/playlists/" + playlistId)
                .build()
            )
            .headers(headers -> headers.setBearerAuth(accessToken))
            .body(new UpdateDetailsRequest(isPublic))
            .retrieve()
            .toBodilessEntity();
    }

    public void setCoverImage(String accessToken, String playlistId, String imageUrl) {
        byte[] imageBytes = restClient.get()
            .uri(imageUrl)
            .retrieve()
            .body(byte[].class);
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        restClient.put()
            .uri(uriBuilder -> uriBuilder
                .scheme("https").host("api.spotify.com").path("/v1/playlists/" + playlistId + "/images")
                .build()
            )
            .headers(headers -> headers.setBearerAuth(accessToken))
            .contentType(MediaType.valueOf("image/jpeg"))
            .body(base64Image)
            .retrieve()
            .toBodilessEntity();
    }

    public void addTrack(String accessToken, String playlistId, String trackUri, int position) {
        restClient.post()
            .uri(uriBuilder -> uriBuilder
                .scheme("https").host("api.spotify.com").path("/v1/playlists/" + playlistId + "/items")
                .build()
            )
            .headers(headers -> headers.setBearerAuth(accessToken))
            .body(new AddTrackRequest(new String[]{trackUri}, position))
            .retrieve()
            .toBodilessEntity();
    }

    public void deleteTrack(String accessToken, String playlistId, String trackUri) {
        restClient.method(HttpMethod.DELETE)
            .uri(uriBuilder -> uriBuilder
                .scheme("https").host("api.spotify.com").path("/v1/playlists/" + playlistId + "/items")
                .build()
            )
            .headers(headers -> headers.setBearerAuth(accessToken))
            .body(new DeleteTrackRequest(new Track[]{new Track(trackUri)}))
            .retrieve()
            .toBodilessEntity();
    }

    private record CreatePlaylistRequest(
        @JsonProperty("name") String name,
        @JsonProperty("public") boolean isPublic,
        @JsonProperty("description") String description
    ) {}

    private record UpdateDetailsRequest(
        @JsonProperty("public") boolean isPublic
    ) {}

    private record CreatePlaylistResponse(
        @JsonProperty("id") String id
    ) {}

    private record AddTrackRequest(
        @JsonProperty("uris") String[] uris,
        @JsonProperty("position") int position
    ) {}

    private record DeleteTrackRequest(
        @JsonProperty("items") Track[] tracks
    ) {}

    private record Track(
        @JsonProperty("uri") String trackUri
    ) {}
}
