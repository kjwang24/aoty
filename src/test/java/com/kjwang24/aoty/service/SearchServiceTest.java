package com.kjwang24.aoty.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.service.SearchClient.Track;
import com.kjwang24.aoty.service.SearchService.SearchResult;

@ExtendWith(MockitoExtension.class)
public class SearchServiceTest {

    @Mock
    private SearchClient searchClient;

    @Mock
    private TokenRefreshService tokenRefreshService;

    @InjectMocks
    private SearchService searchService;

    @Test
    void findTracks_mapsClientResultsToSearchResults() {
        User user = new User();
        user.setAccountId("kjwang24");

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode album = objectMapper.readTree("{\"images\":[{\"url\":\"http://cover\"}]}");
        JsonNode artist = objectMapper.readTree("{\"name\":\"the strokes\"}");
        Track track = new Track("reptilia", "reptilia", album, List.of(artist));

        when(tokenRefreshService.getValidAccessToken(user)).thenReturn("access");
        when(searchClient.search("access", "reptilia")).thenReturn(List.of(track));

        List<SearchResult> results = searchService.findTracks(user, "reptilia");

        assertThat(results).containsExactly(
            new SearchResult("reptilia", "reptilia", "the strokes", "http://cover"));
    }

    @Test
    void findTracks_noMatches_returnsEmptyList() {
        User user = new User();
        user.setAccountId("kjwang24");

        when(tokenRefreshService.getValidAccessToken(user)).thenReturn("access");
        when(searchClient.search("access", "asdkjaslkdj")).thenReturn(List.of());

        List<SearchResult> results = searchService.findTracks(user, "asdkjaslkdj");

        assertThat(results).isEmpty();
    }
}
