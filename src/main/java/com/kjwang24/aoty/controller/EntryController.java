package com.kjwang24.aoty.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.entity.Entry;
import com.kjwang24.aoty.repository.UserRepository;
import com.kjwang24.aoty.service.EntryService;
import com.kjwang24.aoty.service.EntryService.SongSelection;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;
    private final UserRepository userRepository;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<Entry> getAllEntries(@AuthenticationPrincipal OAuth2User principal) {
        User user = resolveUser(principal);
        return entryService.getAllEntries(user);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createEntry(@AuthenticationPrincipal OAuth2User principal, @RequestBody EntryPostRequest request) {
        User user = resolveUser(principal);
        SongSelection song = new SongSelection(request.spotifyId(), request.songName(), request.songArtist(), request.songCoverArt());
        entryService.createEntry(user, request.date(), song, Optional.ofNullable(request.note()));
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{date}")
    public Entry updateEntry(@AuthenticationPrincipal OAuth2User principal, @PathVariable LocalDate date,
            @RequestBody EntryPatchRequest request) {
        User user = resolveUser(principal);
        Optional<SongSelection> song = Optional.ofNullable(request.spotifyId())
                .map(id -> new SongSelection(id, request.songName(), request.songArtist(), request.songCoverArt()));
        return entryService.updateEntry(user, date, song, Optional.ofNullable(request.note()));
    }

    private User resolveUser(OAuth2User principal) {
        String accountId = principal.getAttribute("account_id");
        return userRepository.findByAccountId(accountId).orElseThrow(() -> new IllegalStateException("no user found for that account id " + accountId));
    }

    private record EntryPostRequest(
            @JsonProperty("date") LocalDate date,
            @JsonProperty("spotify_id") String spotifyId,
            @JsonProperty("song_name") String songName,
            @JsonProperty("song_artist") String songArtist,
            @JsonProperty("song_cover_art") String songCoverArt,
            @JsonProperty("note") String note) {
    }

    private record EntryPatchRequest(
            @JsonProperty("spotify_id") String spotifyId,
            @JsonProperty("song_name") String songName,
            @JsonProperty("song_artist") String songArtist,
            @JsonProperty("song_cover_art") String songCoverArt,
            @JsonProperty("note") String note) {
    }

}
