package com.kjwang24.aoty.repository;

import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.entity.ListeningRecord;

import java.util.List;
import java.time.Instant;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ListeningRecordRepository extends JpaRepository<ListeningRecord, Long> {

    Boolean existsByUserAndSpotifyIdAndPlayedAt(User user, String spotifyId, Instant playedAt);

    List<ListeningRecord> findAllByUserAndPlayedAtAfter(User user, Instant playedAt);

    List<ListeningRecord> findTop250ByUserOrderByPlayedAtDesc(User user);

    // A one-element window into this user's plays, newest first. Used to find the timestamp of
    // the Nth most recent play by passing PageRequest.of(n - 1, 1); comes back empty when the
    // user has fewer than n plays.
    @Query("select r.playedAt from ListeningRecord r where r.user = :user order by r.playedAt desc")
    List<Instant> findPlayedAtDescending(@Param("user") User user, Pageable pageable);

    // A bulk delete rather than a derived one: pruning discards hundreds of rows at a time and
    // there is no reason to load them all into the persistence context first.
    @Modifying
    @Transactional
    @Query("delete from ListeningRecord r where r.user = :user and r.playedAt < :cutoff")
    int deleteByUserOlderThan(@Param("user") User user, @Param("cutoff") Instant cutoff);

}
