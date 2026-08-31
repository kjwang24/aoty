package com.kjwang24.aoty.security;

import java.io.IOException;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CookieCsrfTokenRepository resolves the CSRF token lazily — it's only actually written to the
 * response cookie if something reads CsrfToken.getToken() during the request. A pure JSON API has
 * no view layer to trigger that read, so without this filter the XSRF-TOKEN cookie never gets set
 * and every POST/PATCH from the SPA fails CSRF validation.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }

}
