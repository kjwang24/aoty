package com.kjwang24.aoty.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.entity.Entry;
import com.kjwang24.aoty.repository.UserRepository;
import com.kjwang24.aoty.service.EntryService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
public class EntryController {

    private final EntryService entryService;
    private final UserRepository userRepository;
    
    public EntryController(EntryService entryService, UserRepository userRepository) {
        this.entryService = entryService;
        this.userRepository = userRepository;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<Entry> getAllEntries(@AuthenticationPrincipal OAuth2User principal) {
        User user = resolveUser(principal);
        return entryService.getAllEntries(user);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createEntry(@AuthenticationPrincipal OAuth2User principal, @RequestBody EntryRequest request) {
        User user = resolveUser(principal);
        entryService.createEntry(user, request.date(), request.spotifyId(), Optional.ofNullable(request.note()));
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{date}")
    public Entry updateEntry(@AuthenticationPrincipal OAuth2User principal, @PathVariable LocalDate date,
            @RequestBody EntryUpdateRequest request) {
        User user = resolveUser(principal);
        return entryService.updateEntry(user, date, Optional.ofNullable(request.spotifyId()), Optional.ofNullable(request.note()));
    }

    private User resolveUser(OAuth2User principal) {
        String accountId = principal.getAttribute("account_id");
        return userRepository.findByAccountId(accountId).orElseThrow(() -> new IllegalStateException("no user found for that account id " + accountId));
    }

    private record EntryRequest(
            @JsonProperty("date") LocalDate date,
            @JsonProperty("spotify_id") String spotifyId,
            @JsonProperty("note") String note) {
    }

    private record EntryUpdateRequest(
            @JsonProperty("spotify_id") String spotifyId,
            @JsonProperty("note") String note) {
    }

}
