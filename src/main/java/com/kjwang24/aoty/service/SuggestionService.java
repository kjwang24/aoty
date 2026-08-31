package com.kjwang24.aoty.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.kjwang24.aoty.entity.ListeningRecord;
import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.repository.ListeningRecordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuggestionService {

    static final int[] WINDOW_BOUNDS = { 0, 50, 150, 250 }; // 3 suggestions, 1 from each window

    public static final int MAX_USEFUL_HISTORY = WINDOW_BOUNDS[WINDOW_BOUNDS.length - 1];

    private final ListeningRecordRepository listeningRecordRepository;

    public List<Suggestion> suggest(User user) {
        List<ListeningRecord> history = listeningRecordRepository.findTop250ByUserOrderByPlayedAtDesc(user);

        List<Suggestion> suggestions = new ArrayList<>();
        Set<Integer> alreadyPicked = new HashSet<>();
        int fallbackStart = 0;
        int fallbackEnd = 0; // in case user listening history is shorter than window

        for (int i = 0; i + 1 < WINDOW_BOUNDS.length; i++) {
            int start = Math.min(WINDOW_BOUNDS[i], history.size());
            int end = Math.min(WINDOW_BOUNDS[i + 1], history.size());
            if (start < end) {
                fallbackStart = start;
                fallbackEnd = end;
            } else {
                start = fallbackStart;
                end = fallbackEnd;
            }

            Integer index = pickUnpicked(start, end, alreadyPicked);
            if (index == null) {
                continue;
            }
            alreadyPicked.add(index);
            suggestions.add(toSuggestion(history.get(index)));
        }

        return suggestions;
    }

    private Integer pickUnpicked(int start, int end, Set<Integer> alreadyPicked) {
        List<Integer> candidates = new ArrayList<>();
        for (int i = start; i < end; i++) {
            if (!alreadyPicked.contains(i)) {
                candidates.add(i);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    private Suggestion toSuggestion(ListeningRecord record) {
        return new Suggestion(record.getSpotifyId(),
            record.getSongName(),
            record.getSongArtist(),
            record.getSongCoverArt());
    }

    public record Suggestion(
        String spotifyId,
        String songName,
        String songArtist,
        String songCoverArt
    ) {}
}
