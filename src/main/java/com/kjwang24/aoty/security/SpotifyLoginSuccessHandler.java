package com.kjwang24.aoty.security;

import java.io.IOException;

import com.kjwang24.aoty.service.SpotifyAuthService;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpotifyLoginSuccessHandler implements AuthenticationSuccessHandler {
    
    private final SpotifyAuthService spotifyAuthService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oAuthToken = (OAuth2AuthenticationToken) authentication;
        // String registrationId = oAuthToken.getAuthorizedClientRegistrationId();
        OAuth2User user = oAuthToken.getPrincipal();
        String accountId = user.getAttribute("account_id");
        String displayName = user.getAttribute("display_name");
        OAuth2AuthorizedClient tokens = authorizedClientService.loadAuthorizedClient("spotify", accountId);
        spotifyAuthService.handleLogin(accountId, displayName, tokens.getAccessToken().getTokenValue(), tokens.getRefreshToken().getTokenValue(), tokens.getAccessToken().getExpiresAt());
        response.sendRedirect("/");
    }

}
