package com.kjwang24.aoty.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        String accountId = principal.getAttribute("account_id");
        User user = userRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalStateException("no user found for that account id " + accountId));
        return new UserResponse(user.getDisplayName(), user.getPfpUrl());
    }

    private record UserResponse(
        @JsonProperty("display_name") String displayName,
        @JsonProperty("pfp_url") String pfpUrl
    ) {}

}
