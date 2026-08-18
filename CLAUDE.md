# aoty

Spring Boot + React app for building a "year in review" album: user picks one song per day, optionally with a note to their future self, forming a playlist/calendar for the year.

## Stack
- Backend: Spring Boot (Java 17), Postgres, Spring Data JPA, Spring Security OAuth2 (Spotify)
- Frontend: React (separate from this Maven project)
- External: Spotify Web API (OAuth login, recently-played, search, playlists)

## Core rules
- Users select a song per calendar day, from either their scraped listening history or custom search.
- Past days are locked — no editing entries once the day has passed.
- Missed days can be backfilled later (make-up entries), but only for empty/missed days, not to overwrite existing entries.
- Spotify's recently-played API only retains a short history, so a daily scheduled job scrapes and persists it server-side.

## Planned components
- **Entities**: `User`, `SpotifyCredential` (OAuth tokens), `ListeningHistoryEntry` (scraped plays), `DayEntry` (date, song, note, locked/backfilled flag)
- **Spotify OAuth**: Spring Security OAuth2 client flow + token refresh
- **Scheduled scraper**: `@Scheduled` job pulling recently-played tracks daily
- **Search service**: wraps Spotify search API for custom song lookup
- **Entry service**: enforces lock/backfill rules
- **Repositories**: Spring Data JPA over Postgres
- **REST controllers**: calendar data, entry create/backfill, search, suggestions
- **Playlist/export logic**: assembles year's entries into ordered tracklist, optionally synced to a real Spotify playlist for frontend embed

## Frontend (React, separate repo/dir)
- Calendar view of the year's entries
- Search/suggestion picker for daily song choice
- Note input per entry
- Embedded Spotify player/playlist UI

## Communication
Whenever asked for the general steps needed to implement a feature, write them to `~/aoty/TODO.md`