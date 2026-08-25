package com.kjwang24.aoty.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.repository.UserRepository;
import com.kjwang24.aoty.service.SearchService;
import com.kjwang24.aoty.service.SearchService.SearchResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final UserRepository userRepository;
    private final SearchService searchService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping()
    public List<SearchResult> getSearchResults(@AuthenticationPrincipal OAuth2User principal, @RequestParam("q") String query) {
        User user = resolveUser(principal);
        return searchService.findTracks(user, query);
    }

    private User resolveUser(OAuth2User principal) {
        String accountId = principal.getAttribute("account_id");
        return userRepository.findByAccountId(accountId).orElseThrow(() -> new IllegalStateException("no user found for that account id " + accountId));
    }

}
