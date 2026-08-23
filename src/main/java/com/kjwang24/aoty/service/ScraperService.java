package com.kjwang24.aoty.service;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.kjwang24.aoty.entity.ListeningRecord;
import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.repository.ListeningRecordRepository;
import com.kjwang24.aoty.repository.UserRepository;
import com.kjwang24.aoty.service.SpotifyRecentlyPlayedClient.RecentlyPlayedItem;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScraperService {

    private final UserRepository userRepository;
    private final ListeningRecordRepository listeningRecordRepository;
    private final TokenRefreshService tokenRefreshService;
    private final SpotifyRecentlyPlayedClient spotifyRecentlyPlayedClient;

    @Scheduled(cron = "0 0 0,12,18 * * *") // cuz are we really gonna listen to 50+ songs between midnight and noon
    public void scrape() {
        for (User user : userRepository.findAll()) {
            try {
                String accessToken = tokenRefreshService.getValidAccessToken(user);
                List<RecentlyPlayedItem> items = spotifyRecentlyPlayedClient.fetchRecentlyPlayed(accessToken);

                for (RecentlyPlayedItem item : items) {
                    boolean scraped = listeningRecordRepository.existsByUserAndSpotifyIdAndPlayedAt(user, item.track().spotifyId(), item.playedAt());
                    if (!scraped) {
                        listeningRecordRepository.save(toListeningRecord(item, user));
                    }
                }
            }
            catch (Exception e) {
                log.warn("warning: scrape at {} utc failed for user with db id {}: {}", Instant.now(), user.getId(), e.getMessage());
            }
        }
    }

    private ListeningRecord toListeningRecord(RecentlyPlayedItem item, User user) {
        ListeningRecord record = new ListeningRecord();
        record.setUser(user);
        record.setSpotifyId(item.track().spotifyId());
        record.setSongName(item.track().songName());
        record.setSongArtist(item.track().artists().get(0).path("name").asString());
        record.setSongCoverArt(item.track().album().path("images").get(0).path("url").asString());
        record.setPlayedAt(item.playedAt());
        return record;
    }
}
