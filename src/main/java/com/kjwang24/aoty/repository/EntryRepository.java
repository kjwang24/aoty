package com.kjwang24.aoty.repository;

import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.entity.Entry;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    Optional<Entry> findByUserAndDate(User user, LocalDate date);

    List<Entry> findByUserOrderByDateAsc(User user);
}
