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
import com.kjwang24.aoty.service.EntryService.SongSelection;

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
               .content("{\"date\":\"2026-08-01\",\"spotify_id\":\"good days\",\"song_name\":\"Good Days\",\"song_artist\":\"SZA\",\"song_cover_art\":\"http://cover\"}"))
               .andExpect(status().isCreated());

        verify(entryService).createEntry(user, LocalDate.of(2026, 8, 1),
                new SongSelection("good days", "Good Days", "SZA", "http://cover"), Optional.empty());
    }

    @Test
    void postDuplicateEntry_throws409() throws Exception {
        User user = new User();
        user.setAccountId("kjwang24");
        SongSelection song = new SongSelection("didn't cha know", "Didn't Cha Know", "Erykah Badu", "http://cover");

        when(userRepository.findByAccountId("kjwang24")).thenReturn(java.util.Optional.of(user));
        when(entryService.createEntry(user, LocalDate.of(2026, 8, 1), song, Optional.empty()))
                .thenThrow(new DuplicateEntryException(""));

        mockMvc.perform(post("/entries")
               .with(oauth2Login().attributes(attrs -> attrs.put("account_id", "kjwang24")))
               .with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"date\":\"2026-08-01\", \"spotify_id\":\"didn't cha know\",\"song_name\":\"Didn't Cha Know\",\"song_artist\":\"Erykah Badu\",\"song_cover_art\":\"http://cover\"}"))
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

        SongSelection song = new SongSelection("planes", "Planes", "Jeremy Zucker", "http://cover");

        when(userRepository.findByAccountId("kjwang24")).thenReturn(java.util.Optional.of(user));
        when(entryService.updateEntry(user, date, Optional.of(song), Optional.empty())).thenReturn(entry);

        mockMvc.perform(patch("/entries/{date}", date)
               .with(oauth2Login().attributes(attrs -> attrs.put("account_id", "kjwang24")))
               .with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"spotify_id\":\"planes\",\"song_name\":\"Planes\",\"song_artist\":\"Jeremy Zucker\",\"song_cover_art\":\"http://cover\"}"))
               .andExpect(status().isOk());

        verify(entryService).updateEntry(user, date, Optional.of(song), Optional.empty());
    }

    @Test
    void patchInvalidEntry_throws403() throws Exception { // invalid bc it's modifying a past date
        User user = new User();
        user.setAccountId("kjwang24");
        LocalDate date = LocalDate.of(2026, 8, 1);
        SongSelection song = new SongSelection("400 lux", "400 Lux", "Lorde", "http://cover");

        when(userRepository.findByAccountId("kjwang24")).thenReturn(java.util.Optional.of(user));
        when(entryService.updateEntry(user, date, Optional.of(song), Optional.empty()))
                .thenThrow(new ForbiddenUpdateException(""));

        mockMvc.perform(patch("/entries/{date}", date)
               .with(oauth2Login().attributes(attrs -> attrs.put("account_id", "kjwang24")))
               .with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"spotify_id\":\"400 lux\",\"song_name\":\"400 Lux\",\"song_artist\":\"Lorde\",\"song_cover_art\":\"http://cover\"}"))
               .andExpect(status().isForbidden());
    }

}
