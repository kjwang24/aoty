package com.kjwang24.aoty.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kjwang24.aoty.entity.Entry;
import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.repository.UserRepository;
import com.kjwang24.aoty.service.EntryExceptions.DuplicateEntryException;
import com.kjwang24.aoty.service.EntryExceptions.ForbiddenUpdateException;
import com.kjwang24.aoty.service.EntryService;

@WebMvcTest(EntryController.class)
public class EntryControllerTest {

    @MockitoBean
    private EntryService entryService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllEntries_returnsExistingEntries() throws Exception {
        User user = new User();
        user.setAccountId("kjwang24");

        Entry first = new Entry();
        first.setUser(user);
        first.setDate(LocalDate.of(2026, 8, 1));
        first.setSpotifyId("strawberry swing");

        Entry second = new Entry();
        second.setUser(user);
        second.setDate(LocalDate.of(2026, 8, 2));
        second.setSpotifyId("desol");

        when(userRepository.findByAccountId("kjwang24")).thenReturn(java.util.Optional.of(user));
        when(entryService.getAllEntries(user)).thenReturn(List.of(first, second));

        mockMvc.perform(get("/entries")
               .with(oauth2Login().attributes(attrs -> attrs.put("account_id", "kjwang24"))))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(content().json("[{\"date\":\"2026-08-01\",\"spotifyId\":\"strawberry swing\"}, {\"date\":\"2026-08-02\",\"spotifyId\":\"desol\"}]"));
    }

    @Test
    void postValidEntry_createsNewEntry() throws Exception {
        User user = new User();
        user.setAccountId("kjwang24");

        when(userRepository.findByAccountId("kjwang24")).thenReturn(java.util.Optional.of(user));

        mockMvc.perform(post("/entries")
               .with(oauth2Login().attributes(attrs -> attrs.put("account_id", "kjwang24")))
               .with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"date\":\"2026-08-01\",\"spotify_id\":\"good days\"}"))
               .andExpect(status().isCreated());

        verify(entryService).createEntry(user, LocalDate.of(2026, 8, 1), "good days", Optional.empty());
    }

    @Test
    void postDuplicateEntry_throws409() throws Exception {
        User user = new User();
        user.setAccountId("kjwang24");

        when(userRepository.findByAccountId("kjwang24")).thenReturn(java.util.Optional.of(user));
        when(entryService.createEntry(user, LocalDate.of(2026, 8, 1), "didn't cha know", Optional.empty()))
                .thenThrow(new DuplicateEntryException(""));

        mockMvc.perform(post("/entries")
               .with(oauth2Login().attributes(attrs -> attrs.put("account_id", "kjwang24")))
               .with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"date\":\"2026-08-01\", \"spotify_id\":\"didn't cha know\"}"))
               .andExpect(status().isConflict());
    }

    @Test
    void patchValidEntry_updatesEntry() throws Exception {
        User user = new User();
        user.setAccountId("kjwang24");

        LocalDate date = LocalDate.now();
        Entry entry = new Entry();
        entry.setUser(user);
        entry.setDate(date);
        entry.setSpotifyId("planes");

        when(userRepository.findByAccountId("kjwang24")).thenReturn(java.util.Optional.of(user));
        when(entryService.updateEntry(user, date, Optional.of("planes"), Optional.empty())).thenReturn(entry);

        mockMvc.perform(patch("/entries/{date}")
               .with(oauth2Login().attributes(attrs -> attrs.put("account_id", "kjwang24")))
               .with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content(String.format("{\"date\":\"%s\",\"spotify_id\":\"planes\"}", date)))
               .andExpect(status().isOk());

        verify(entryService).updateEntry(user, date, Optional.of("planes"), Optional.empty());
    }

    @Test
    void patchInvalidEntry_throws403() throws Exception { // invalid bc it's modifying a past date
        User user = new User();
        user.setAccountId("kjwang24");
        LocalDate date = LocalDate.of(2026, 8, 1);

        when(userRepository.findByAccountId("kjwang24")).thenReturn(java.util.Optional.of(user));
        when(entryService.updateEntry(user, date, Optional.of("400 lux"), Optional.empty()))
                .thenThrow(new ForbiddenUpdateException(""));

        mockMvc.perform(patch("/entries/{date}")
               .with(oauth2Login().attributes(attrs -> attrs.put("account_id", "kjwang24")))
               .with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"date\":\"2026-08-01\", \"spotify_id\":\"400 lux\"}"))
               .andExpect(status().isForbidden());
    }

}
