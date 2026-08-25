package com.kjwang24.aoty.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.repository.UserRepository;
import com.kjwang24.aoty.service.PlaylistService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/playlist")
@RequiredArgsConstructor
public class PlaylistController {
    
    private final UserRepository userRepository;
    private final PlaylistService playlistService;

    @GetMapping
    public ResponseEntity<String> getPlaylistUrl(@AuthenticationPrincipal OAuth2User principal) {
        User user = resolveUser(principal);
        Optional<String> url = playlistService.getPlaylistUrl(user);
        return url.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.noContent().build());
    }

    private User resolveUser(OAuth2User principal) {
        String accountId = principal.getAttribute("account_id");
        return userRepository.findByAccountId(accountId).orElseThrow(() -> new IllegalStateException("no user found for that account id " + accountId));
    }

}
