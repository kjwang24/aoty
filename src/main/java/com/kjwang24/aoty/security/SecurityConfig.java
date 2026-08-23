package com.kjwang24.aoty.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SpotifyLoginSuccessHandler spotifyLoginSuccessHandler) throws Exception {
        http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
            .authorizeHttpRequests(auth -> auth.requestMatchers("/oauth2/**", "/login/**").permitAll()
                                               .anyRequest().authenticated())
            .exceptionHandling(handling -> handling.defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), 
                PathPatternRequestMatcher.withDefaults().matcher("/entries/**")))
            .oauth2Login(oauth2 -> oauth2.successHandler(spotifyLoginSuccessHandler));

        return http.build();
    }

}
