package com.kjwang24.aoty.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kjwang24.aoty.entity.Entry;
import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.repository.EntryRepository;
import com.kjwang24.aoty.service.EntryExceptions.DuplicateEntryException;
import com.kjwang24.aoty.service.EntryExceptions.ForbiddenUpdateException;
import com.kjwang24.aoty.service.EntryService.SongSelection;

@ExtendWith(MockitoExtension.class)
public class EntryServiceTest {

    @Mock
    private EntryRepository entryRepository;

    @Mock
    private PlaylistService playlistService;

    @InjectMocks
    private EntryService entryService;

    @Test
    void createEntry_succeeds_forPastDayWithNoExistingEntry() {
        User user = new User();
        user.setAccountId("kjwang24");
        LocalDate date = LocalDate.of(2026, 8, 1);
        SongSelection song = new SongSelection("slug", "Slug", "Youth Lagoon", "http://cover");

        when(entryRepository.findByUserAndDate(user, date)).thenReturn(Optional.empty());
        when(entryRepository.save(any(Entry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Entry result = entryService.createEntry(user, date, song, Optional.empty());

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getDate()).isEqualTo(date);
        assertThat(result.getSpotifyId()).isEqualTo("slug");
        assertThat(result.getSongName()).isEqualTo("Slug");
        assertThat(result.getSongArtist()).isEqualTo("Youth Lagoon");
        assertThat(result.getSongCoverArt()).isEqualTo("http://cover");
        verify(playlistService).syncEntry(user, result, Optional.empty());
    }

    @Test
    void createEntry_fails_whenEntryAlreadyExists() {
        User user = new User();
        user.setAccountId("kjwang24");
        LocalDate date = LocalDate.of(2026, 8, 1);

        Entry original = new Entry();
        original.setUser(user);
        original.setDate(date);
        original.setSpotifyId("cherry");

        when(entryRepository.findByUserAndDate(user, date)).thenReturn(Optional.of(original));

        SongSelection song = new SongSelection("pier 4", "Pier 4", "Now, Now", "http://cover");

        assertThatThrownBy(() -> entryService.createEntry(user, date, song, Optional.empty()))
                           .isInstanceOf(DuplicateEntryException.class);
    }

    @Test
    void updateEntry_fails_forPastDaysAndTodaysMissingEntry() {
        User user = new User();
        user.setAccountId("kjwang24");
        LocalDate pastDate = LocalDate.of(2026, 8, 1);

        assertThatThrownBy(() -> entryService.updateEntry(user, pastDate, Optional.empty(), Optional.empty()))
                           .isInstanceOf(ForbiddenUpdateException.class);
    }

    @Test
    void updateEntry_fails_forTodaysMissingEntry() {
        User user = new User();
        user.setAccountId("kjwang24");
        LocalDate today = LocalDate.now();

        when(entryRepository.findByUserAndDate(user, today)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entryService.updateEntry(user, today, Optional.empty(), Optional.empty()))
                           .isInstanceOf(ForbiddenUpdateException.class);
    }

    @Test
    void updateEntry_succeeds_forTodaysExistingEntry() {
        User user = new User();
        user.setAccountId("kjwang24");
        LocalDate today = LocalDate.now();

        Entry original = new Entry();
        original.setUser(user);
        original.setDate(today);
        original.setSpotifyId("always with me");

        when(entryRepository.findByUserAndDate(user, today)).thenReturn(Optional.of(original));
        when(entryRepository.save(any(Entry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SongSelection song = new SongSelection("mori no chiisana restaurant", "Mori no Chiisana Restaurant", "Ghibli", "http://cover");
        Entry updated = entryService.updateEntry(user, today, Optional.of(song), Optional.of("smol"));

        assertThat(updated.getSpotifyId()).isEqualTo("mori no chiisana restaurant");
        assertThat(updated.getNote()).isEqualTo("smol");
        verify(playlistService).syncEntry(user, updated, Optional.of("always with me"));
    }

    @Test
    void updateEntry_doesNotTouchPlaylist_whenOnlyTheNoteChanges() {
        User user = new User();
        user.setAccountId("kjwang24");
        LocalDate today = LocalDate.now();

        Entry original = new Entry();
        original.setUser(user);
        original.setDate(today);
        original.setSpotifyId("always with me");

        when(entryRepository.findByUserAndDate(user, today)).thenReturn(Optional.of(original));
        when(entryRepository.save(any(Entry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Entry updated = entryService.updateEntry(user, today, Optional.empty(), Optional.of("for later"));

        assertThat(updated.getSpotifyId()).isEqualTo("always with me");
        assertThat(updated.getNote()).isEqualTo("for later");
        verify(playlistService, never()).syncEntry(any(), any(), any());
    }

}
