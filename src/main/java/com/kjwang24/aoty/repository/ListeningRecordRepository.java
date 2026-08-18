package com.kjwang24.aoty.repository;

import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.entity.ListeningRecord;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ListeningRecordRepository extends JpaRepository<ListeningRecord, Long> {
    
    Boolean existsByUserAndSpotifyIdAndPlayedAt(User user, String spotifyId, LocalDateTime playedAt);

    List<ListeningRecord> findAllByUserAndPlayedAtAfter(User user, LocalDateTime playedAt);

}
