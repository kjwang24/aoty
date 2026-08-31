package com.kjwang24.aoty.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kjwang24.aoty.entity.ListeningRecord;
import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.repository.ListeningRecordRepository;
import com.kjwang24.aoty.service.SuggestionService.Suggestion;

@ExtendWith(MockitoExtension.class)
public class SuggestionServiceTest {

    @Mock
    private ListeningRecordRepository listeningRecordRepository;

    @InjectMocks
    private SuggestionService suggestionService;

    private final User user = new User();

    /**
     * A history of {@code size} records whose spotifyId is its own index, so a suggestion's id
     * says exactly how many plays ago it was and can be checked against a window's bounds.
     */
    private void givenHistoryOfSize(int size) {
        List<ListeningRecord> history = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ListeningRecord record = new ListeningRecord();
            record.setSpotifyId(String.valueOf(i));
            record.setSongName("song " + i);
            record.setSongArtist("artist " + i);
            record.setSongCoverArt("http://cover/" + i);
            record.setPlayedAt(Instant.now().minusSeconds(i));
            history.add(record);
        }
        when(listeningRecordRepository.findTop250ByUserOrderByPlayedAtDesc(user)).thenReturn(history);
    }

    private static int playsAgo(Suggestion suggestion) {
        return Integer.parseInt(suggestion.spotifyId());
    }

    @Test
    void suggest_drawsOneFromEachWindow_whenHistoryIsFull() {
        givenHistoryOfSize(250);

        List<Suggestion> suggestions = suggestionService.suggest(user);

        assertThat(suggestions).hasSize(3);
        assertThat(playsAgo(suggestions.get(0))).isBetween(0, 49);
        assertThat(playsAgo(suggestions.get(1))).isBetween(50, 149);
        assertThat(playsAgo(suggestions.get(2))).isBetween(150, 249);
    }

    @Test
    void suggest_neverReachesPastTheLastWindow_whenHistoryIsLongerThanItNeedsToBe() {
        givenHistoryOfSize(400);

        List<Suggestion> suggestions = suggestionService.suggest(user);

        assertThat(suggestions).allSatisfy(s -> assertThat(playsAgo(s)).isLessThan(250));
    }

    @Test
    void suggest_shortensLaterWindows_whenHistoryRunsOutPartway() {
        // The worked example: 100 records means the third window has nothing of its own, so the
        // second and third suggestions both come out of the 50th-100th, as two distinct songs.
        givenHistoryOfSize(100);

        List<Suggestion> suggestions = suggestionService.suggest(user);

        assertThat(suggestions).hasSize(3);
        assertThat(playsAgo(suggestions.get(0))).isBetween(0, 49);
        assertThat(playsAgo(suggestions.get(1))).isBetween(50, 99);
        assertThat(playsAgo(suggestions.get(2))).isBetween(50, 99);
        assertThat(suggestions).extracting(Suggestion::spotifyId).doesNotHaveDuplicates();
    }

    @Test
    void suggest_drawsAllThreeFromTheFirstWindow_whenThatIsAllThereIs() {
        givenHistoryOfSize(10);

        List<Suggestion> suggestions = suggestionService.suggest(user);

        assertThat(suggestions).hasSize(3);
        assertThat(suggestions).allSatisfy(s -> assertThat(playsAgo(s)).isBetween(0, 9));
        assertThat(suggestions).extracting(Suggestion::spotifyId).doesNotHaveDuplicates();
    }

    @Test
    void suggest_returnsWhatItCan_whenThereAreFewerRecordsThanWindows() {
        givenHistoryOfSize(2);

        List<Suggestion> suggestions = suggestionService.suggest(user);

        assertThat(suggestions).hasSize(2);
        assertThat(suggestions).extracting(Suggestion::spotifyId).containsExactlyInAnyOrder("0", "1");
    }

    @Test
    void suggest_returnsNothing_forAUserWithNoListeningHistory() {
        givenHistoryOfSize(0);

        assertThat(suggestionService.suggest(user)).isEmpty();
    }

    @Test
    void suggest_carriesTheSongDetailsThrough() {
        givenHistoryOfSize(1);

        Suggestion suggestion = suggestionService.suggest(user).get(0);

        assertThat(suggestion.songName()).isEqualTo("song 0");
        assertThat(suggestion.songArtist()).isEqualTo("artist 0");
        assertThat(suggestion.songCoverArt()).isEqualTo("http://cover/0");
    }
}
