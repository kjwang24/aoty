package com.kjwang24.aoty.repository;

import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.entity.ListeningRecord;

import java.util.List;
import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ListeningRecordRepository extends JpaRepository<ListeningRecord, Long> {

    Boolean existsByUserAndSpotifyIdAndPlayedAt(User user, String spotifyId, Instant playedAt);

    List<ListeningRecord> findAllByUserAndPlayedAtAfter(User user, Instant playedAt);

}
