package com.kjwang24.aoty.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.repository.UserRepository;
import com.kjwang24.aoty.service.SearchService;
import com.kjwang24.aoty.service.SearchService.SearchResult;

@WebMvcTest(SearchController.class)
public class SearchControllerTest {

    @MockitoBean
    private SearchService searchService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getSearchResults_returnsMatches() throws Exception {
        User user = new User();
        user.setAccountId("kjwang24");

        SearchResult result = new SearchResult("vienna", "vienna", "billy joel", "http://cover");

        when(userRepository.findByAccountId("kjwang24")).thenReturn(Optional.of(user));
        when(searchService.findTracks(user, "vienna")).thenReturn(List.of(result));

        mockMvc.perform(get("/search").param("q", "vienna")
               .with(oauth2Login().attributes(attrs -> attrs.put("account_id", "kjwang24"))))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(content().json("[{\"spotifyId\":\"vienna\",\"songName\":\"vienna\",\"songArtist\":\"billy joel\",\"songCoverArt\":\"http://cover\"}]"));
    }

    @Test
    void getSearchResults_noMatches_returnsEmptyList() throws Exception {
        User user = new User();
        user.setAccountId("kjwang24");

        when(userRepository.findByAccountId("kjwang24")).thenReturn(Optional.of(user));
        when(searchService.findTracks(user, "paper doll")).thenReturn(List.of());

        mockMvc.perform(get("/search").param("q", "paper doll")
               .with(oauth2Login().attributes(attrs -> attrs.put("account_id", "kjwang24"))))
               .andExpect(status().isOk())
               .andExpect(content().json("[]"));
    }
}
