package com.kjwang24.aoty.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.ArrayList;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.entity.ListeningRecord;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ListeningRecordRepositoryTest {

    @Autowired
    private ListeningRecordRepository listeningRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void existsByUserAndSpotifyIdAndPlayedAt_returnsBoolean() {
        User user = new User();
        user.setAccountId("kjwang24");
        userRepository.save(user);

        ListeningRecord record = new ListeningRecord();
        record.setUser(user);
        Instant time = Instant.now();
        record.setPlayedAt(time);
        record.setSpotifyId("prom");
        listeningRecordRepository.save(record);

        Boolean status = listeningRecordRepository.existsByUserAndSpotifyIdAndPlayedAt(user, "prom", time);
        assertThat(status).isTrue();
    }

    @Test
    void findAllByUserAndPlayedAtAfter_returnsCorrectUser() {
        User user1 = new User();
        user1.setAccountId("user1");
        userRepository.save(user1);

        User user2 = new User();
        user2.setAccountId("user2");
        userRepository.save(user2);

        ListeningRecord listeningRecord1 = new ListeningRecord();
        listeningRecord1.setUser(user1);
        listeningRecord1.setPlayedAt(Instant.parse("2026-08-02T00:00:00Z"));
        listeningRecordRepository.save(listeningRecord1);

        ListeningRecord listeningRecord2 = new ListeningRecord();
        listeningRecord2.setUser(user2);
        listeningRecord2.setPlayedAt(Instant.parse("2026-08-02T00:00:00Z"));
        listeningRecordRepository.save(listeningRecord2);

        List<ListeningRecord> records = listeningRecordRepository.findAllByUserAndPlayedAtAfter(user1, Instant.parse("2026-08-01T00:00:00Z"));

        assertThat(records).isEqualTo(List.of(listeningRecord1));
    }

    @Test
    void findAllByUserAndPlayedAtAfter_returnsLaterRecords() {
        User user = new User();
        user.setAccountId("kjwang24");
        userRepository.save(user);

        ListeningRecord early1 = new ListeningRecord();
        early1.setUser(user);
        early1.setSongName("pilot jones");
        early1.setPlayedAt(Instant.parse("2026-07-15T13:00:00Z"));
        listeningRecordRepository.save(early1);

        ListeningRecord late1 = new ListeningRecord();
        late1.setUser(user);
        late1.setSongName("swim between trees");
        late1.setPlayedAt(Instant.parse("2026-08-10T13:00:00Z"));
        listeningRecordRepository.save(late1);

        ListeningRecord late2 = new ListeningRecord();
        late2.setUser(user);
        late2.setSongName("going kokomo");
        late2.setPlayedAt(Instant.parse("2026-08-11T13:00:00Z"));
        listeningRecordRepository.saveAndFlush(late2);

        Instant cutoff = Instant.parse("2026-08-01T00:00:00Z");

        List<ListeningRecord> records = listeningRecordRepository.findAllByUserAndPlayedAtAfter(user, cutoff);
        List<String> recordNames = new ArrayList<String>(records.stream()
                                   .map(ListeningRecord::getSongName)
                                   .toList());
        recordNames.sort(null);

        List<String> expectedRecordNames = List.of("going kokomo", "swim between trees");

        assertThat(recordNames).isEqualTo(expectedRecordNames);
    }

    @Test
    void findByUserAndSpotifyIdAndPlayedAt_noDuplicateEntries() {
        User user = new User();
        user.setAccountId("kjwang24");
        userRepository.save(user);

        Instant time = Instant.parse("2026-08-01T00:00:00Z");

        ListeningRecord original = new ListeningRecord();
        original.setUser(user);
        original.setSpotifyId("superscar");
        original.setPlayedAt(time);
        listeningRecordRepository.saveAndFlush(original);

        ListeningRecord duplicate = new ListeningRecord();
        duplicate.setUser(user);
        duplicate.setSpotifyId("superscar");
        duplicate.setPlayedAt(time);

        assertThatThrownBy(() -> listeningRecordRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
