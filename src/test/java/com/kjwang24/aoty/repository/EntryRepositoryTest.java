package com.kjwang24.aoty.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.entity.Entry;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EntryRepositoryTest {

    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUserAndDate_returnsSavedEntry() {
        User user = new User();
        user.setUsername("kjwang24");
        userRepository.save(user);

        Entry entry = new Entry();
        entry.setUser(user);
        LocalDate date = LocalDate.now();
        entry.setDate(date);
        entryRepository.save(entry);

        Optional<Entry> result = entryRepository.findByUserAndDate(user, date);
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getUsername()).isEqualTo("kjwang24");
        assertThat(result.get().getDate()).isEqualTo(date);
    }

    @Test
    void findByUserAndDate_noDuplicateEntries() {
        User user = new User();
        user.setUsername("kjwang24");
        userRepository.save(user);

        LocalDate date = LocalDate.now();

        Entry original = new Entry();
        original.setUser(user);
        original.setDate(date);
        entryRepository.saveAndFlush(original);

        Entry duplicate = new Entry();
        duplicate.setUser(user);
        duplicate.setDate(date);

        assertThatThrownBy(() -> entryRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    /* not sure if i need to be able to get all of a user's entries
    @Test
    void findByUsername_returnsAllEntries() {
        User user = new User();
        user.setUsername("kjwang24");

        Entry yesterday = new Entry();
        yesterday.setDate(LocalDate.of(2026, 8, 14));
        yesterday.setUser(user);
        entryRepository.save(yesterday);
        Entry today = new Entry();
        today.setDate(LocalDate.of(2026, 8, 15));
        yesterday.setUser(user);
        entryRepository.save(today);
        
        entryRepository.findAllById(null)
    }
    */
}
