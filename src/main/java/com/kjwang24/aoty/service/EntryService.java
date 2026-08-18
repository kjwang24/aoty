package com.kjwang24.aoty.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kjwang24.aoty.entity.Entry;
import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.repository.EntryRepository;

@Service
public class EntryService {

    private final EntryRepository entryRepository;

    public EntryService(EntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    public Entry createEntry(User user, LocalDate date, String spotifyId, Optional<String> note) {
        if (date.isAfter(LocalDate.now())) {
            throw new ForbiddenUpdateException("cannot create entries for future days");
        }
        if (entryRepository.findByUserAndDate(user, date).isPresent()) {
            throw new DuplicateEntryException("an entry already exists for " + user.getUsername() + " on " + date);
        }
        Entry entry = new Entry();
        entry.setUser(user);
        entry.setDate(date);
        entry.setSpotifyId(spotifyId);
        note.ifPresent(entry::setNote);
        return entryRepository.save(entry);
    }

    public Entry updateEntry(User user, LocalDate date, Optional<String> spotifyId, Optional<String> note) {
        if (!date.isEqual(LocalDate.now())) {
            throw new ForbiddenUpdateException("cannot edit the entries of days other than today");
        }
        Entry entry = entryRepository.findByUserAndDate(user, date)
                      .orElseThrow(() -> new ForbiddenUpdateException("no entry exists yet for" + user.getUsername() + "today"));
        spotifyId.ifPresent(entry::setSpotifyId);
        note.ifPresent(entry::setNote);
        return entryRepository.save(entry);
    }

}
