package com.kjwang24.aoty.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

@ExtendWith(MockitoExtension.class)
public class EntryServiceTest {

    @Mock
    private EntryRepository entryRepository;

    @InjectMocks
    private EntryService entryService;

    @Test
    void createEntry_succeeds_forPastDayWithNoExistingEntry() {
        User user = new User();
        user.setAccountId("kjwang24");
        LocalDate date = LocalDate.of(2026, 8, 1);

        when(entryRepository.findByUserAndDate(user, date)).thenReturn(Optional.empty());
        when(entryRepository.save(any(Entry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Entry result = entryService.createEntry(user, date, "slug", Optional.empty());

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getDate()).isEqualTo(date);
        assertThat(result.getSpotifyId()).isEqualTo("slug");
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

        assertThatThrownBy(() -> entryService.createEntry(user, date, "pier 4", Optional.empty()))
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

        Entry updated = entryService.updateEntry(user, today, Optional.of("mori no chiisana restaurant"), Optional.of("smol"));

        assertThat(updated.getSpotifyId()).isEqualTo("mori no chiisana restaurant");
        assertThat(updated.getNote()).isEqualTo("smol");
    }

}
