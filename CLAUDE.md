# aoty

Spring Boot + React app for building a "year in review" album: user picks one song per day, optionally with a note to their future self, forming a playlist/calendar for the year.

## Stack
- Backend: Spring Boot 4.1.0 (Java 17/21), Postgres, Spring Data JPA, Spring Security OAuth2 (Spotify)
- Frontend: React (separate from this Maven project, not started yet)
- External: Spotify Web API (OAuth login, recently-played, search, playlists)

## Core rules
- Users select a song per calendar day, from either their scraped listening history or custom search.
- Past days are locked — no editing entries once the day has passed. Today's entry can still be edited.
- Missed days can be backfilled later, but only for empty/missed days, not to overwrite existing entries.
- Spotify's recently-played API only retains a short history, so a daily scheduled job scrapes and persists it server-side.
- The year's Spotify playlist is maintained live, not assembled once at year's end — every `Entry` create/update immediately syncs that single track into a real Spotify playlist (created lazily on first sync, then reused via `User.spotifyPlaylistId`).

## Current state (entities/repos/services all built + tested)
- **Entities**: `User` (id, accountId [unique, = Spotify's `account_id`], displayName, entries, listeninghistory), `Entry` (id, date, spotifyId, songName, songArtist, songCoverArt, note, user — unique constraint on user_id+date), `ListeningRecord` (table name `history`; unique constraint on user_id+spotifyId+playedAt; `playedAt` is `Instant`, not `LocalDateTime` — it's a UTC point in time from Spotify, not a wall-clock/calendar concept), `SpotifyCredential` (accessToken/refreshToken as TEXT columns — varchar(255) truncated real tokens, user is @OneToOne unique).
- **Repositories**: standard Spring Data interfaces, one test class each using `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` against the real local Postgres (not H2) — needs Postgres actually running.
- **Services**: `EntryService` (create/update/getAllEntries, enforces lock/backfill rules, throws `EntryExceptions.DuplicateEntryException`/`ForbiddenUpdateException`, both nested in one file; also calls `PlaylistService.syncEntry` after every create/update), `SpotifyAuthService` (`handleLogin` — find-or-create User + save/update SpotifyCredential), `TokenRefreshService` (`getValidAccessToken` — checks expiry, only calls Spotify's token endpoint if actually expired, no proactive/scheduled refresh job), `ScraperService`/`ScraperClient` (`@Scheduled` every 6h, loops all users, per-user try/catch so one user's failure doesn't abort the run, skips already-scraped records via `existsByUserAndSpotifyIdAndPlayedAt`; client kept separate so it's mockable without touching `RestClient`'s fluent builder chain), `SearchService`/`SearchClient` (same client/service split, wraps `GET /v1/search?q=...&type=track`), `PlaylistService`/`PlaylistClient` (live playlist sync — see Core rules; `PlaylistClient` uses Spotify's current `/playlists/{id}/items` endpoints, not the deprecated `/tracks` ones; track position = rank among the user's existing entries via `EntryRepository.countByUserAndDateLessThan`, not day-of-year, since backfill can create gaps).
- **Security**: `SecurityConfig` is locked down — `.anyRequest().authenticated()` with `/oauth2/**`+`/login/**` carved out via `permitAll()`, `CookieCsrfTokenRepository.withHttpOnlyFalse()` for the SPA, and `/entries/**`+`/search/**` return a clean 401 via `defaultAuthenticationEntryPointFor` instead of redirecting into the OAuth flow (the default `oauth2Login()` entry point redirects, which is wrong for a JS `fetch` call). `SpotifyLoginSuccessHandler` (`@Component`) wires up the login flow. OAuth flow manually verified working end-to-end through a real browser.
- **Controller**: `EntryController` has GET/POST/PATCH `/entries`, `SearchController` has GET `/search?q=...`, `PlaylistController` has GET `/playlist` (200 + URL if synced, 204 if not yet) — all resolve the authenticated `User` via `@AuthenticationPrincipal OAuth2User` + `UserRepository.findByAccountId`. `GlobalExceptionHandler` maps `DuplicateEntryException`→409, `ForbiddenUpdateException`→403.
- **Backend is functionally complete** (34/34 tests passing) — entities, security, scraper, search, live playlist sync, and the playlist link endpoint are all built and tested. Only the React frontend remains.
- **Immediate next task**: see TODO.md — scaffold the React frontend and get login-detection working end-to-end (not the full UI yet).

## Key decisions/gotchas (not obvious from reading the code alone)
- Use `account_id` (Spotify's stable, immutable identifier), not `id` — Spotify's own docs warn `id` can change over an account's lifetime; `account_id` is what `user-name-attribute` is set to in `application.properties` and what `User.accountId` stores. Don't confuse this with `Entry`/`ListeningRecord`'s `spotifyId` fields, which identify *songs*, not accounts.
- Plain Authorization Code grant (no PKCE) is correct here, not a shortcut — this app's backend is a confidential client (holds `client-secret` server-side), so PKCE (built for public clients that can't hold a secret) isn't needed.
- Spotify no longer allows `localhost` as a redirect URI hostname, only the loopback IP literal `127.0.0.1` — `application.properties` and the Spotify Developer Dashboard app must both use `127.0.0.1`.
- Dev environment is WSL2; Chrome on Windows couldn't reach `127.0.0.1:8080` (WSL2 only auto-forwards the `localhost` hostname, not the literal IP) — fixed via `networkingMode=mirrored` in `.wslconfig`, requiring a `wsl --shutdown` + restart.
- Spring Boot 4 split test-slice support into separate starter artifacts with moved packages: `spring-boot-starter-data-jpa-test` (`org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`, `org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase`), `spring-boot-starter-webmvc-test`, `spring-boot-starter-security-test` (needed for `oauth2Login()`/`csrf()` MockMvc helpers). Not the old `spring-boot-starter-test`-only world.
- Secrets (`SPOTIFY_CLIENT_ID`/`SPOTIFY_CLIENT_SECRET`) live in a gitignored `.env` at the project root — must `source .env` before running the app or the full test suite (`ApplicationTests` boots the whole context and needs them resolved).
- Testing conventions: repositories → `@DataJpaTest` against real Postgres; services → plain Mockito (`@Mock`/`@InjectMocks`/`ArgumentCaptor`), no Spring context; controllers → `@WebMvcTest` + `@MockitoBean` for the service/repo dependencies + `oauth2Login()`/`csrf()`.

## Planned components (not yet built)
- React frontend: project scaffold + login detection first (in progress, see TODO.md), then calendar view, search/suggestion picker, note input, embedded Spotify player

## Frontend (React, separate repo/dir, not started)
- Calendar view of the year's entries
- Search/suggestion picker for daily song choice
- Note input per entry
- Embedded Spotify player/playlist UI

## Communication
Whenever asked for the general steps needed to implement a feature, write them to `~/aoty/TODO.md`.
