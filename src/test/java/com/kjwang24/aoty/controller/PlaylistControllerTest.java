package com.kjwang24.aoty.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.repository.UserRepository;
import com.kjwang24.aoty.service.PlaylistService;

@WebMvcTest(PlaylistController.class)
public class PlaylistControllerTest {

    @MockitoBean
    private PlaylistService playlistService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getExistingPlaylistUrl_returns200() throws Exception {
        User user = new User();
        user.setAccountId("kjwang24");

        when(userRepository.findByAccountId("kjwang24")).thenReturn(Optional.of(user));
        when(playlistService.getPlaylistUrl(user)).thenReturn(Optional.of("https://open.spotify.com/playlist/playlist"));

        mockMvc.perform(get("/playlist")
               .with(oauth2Login().attributes(attrs -> attrs.put("account_id", "kjwang24"))))
               .andExpect(content().string("https://open.spotify.com/playlist/playlist"))
               .andExpect(status().isOk());
    }

    @Test
    void getEmptyPlaylistUrl_returns204() throws Exception {
        User user = new User();
        user.setAccountId("kjwang24");

        when(userRepository.findByAccountId("kjwang24")).thenReturn(Optional.of(user));
        when(playlistService.getPlaylistUrl(user)).thenReturn(Optional.empty());

        mockMvc.perform(get("/playlist")
               .with(oauth2Login().attributes(attrs -> attrs.put("account_id", "kjwang24"))))
               .andExpect(content().string(""))
               .andExpect(status().isNoContent());
    }

}
