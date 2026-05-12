package be.technifutur.tournament.bl;

import be.technifutur.tournament.dl.entity.Match;
import be.technifutur.tournament.dl.entity.Player;
import be.technifutur.tournament.dl.entity.Tournament;
import be.technifutur.tournament.dl.enums.BracketStage;
import be.technifutur.tournament.dl.enums.MatchStatus;
import be.technifutur.tournament.dl.enums.TournamentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BracketGenerationServiceTest {

    private BracketGenerationService service;
    private Tournament tournament;

    @BeforeEach
    void setUp() {
        service = new BracketGenerationService();
        tournament = Tournament.builder()
                               .name("Test")
                               .status(TournamentStatus.DRAFT)
                               .build();
    }

    private List<Player> players(int count) {
        return IntStream.rangeClosed(1, count)
                        .mapToObj(i -> Player.builder()
                                             .username("p" + i)
                                             .build())
                        .toList();
    }

    private static Stream<Arguments> getMetricMatching() {
        return Stream.of(Arguments.of(8, 7, "winner bracket", BracketStage.WINNERS_BRACKET),
                         Arguments.of(8, 6, "loser bracket", BracketStage.LOSERS_BRACKET),
                         Arguments.of(8, 1, "grand final", BracketStage.GRAND_FINAL));
    }

    @ParameterizedTest(name = "From {0} players we expect to have {1} {2}(s)")
    @MethodSource("getMetricMatching")
    void xPlayers_haveCorrectBracketMatchCount(int playerCount, int expected, String ignored, Object stage) {
        var matches = service.generate(tournament, players(playerCount));
        long bracket = matches.stream()
                              .filter(m -> m.getBracketStage() == stage)
                              .count();
        assertEquals(expected, bracket);
    }

    @Test
    void eightPlayers_generates14Matches() {
        var matches = service.generate(tournament, players(8));
        assertEquals(14, matches.size());
    }

    @Test
    void fourPlayers_generates6Matches() {
        var matches = service.generate(tournament, players(4));
        assertEquals(6, matches.size());
    }

    @Test
    void wbR1Matches_havePlayersAssigned() {
        var matches = service.generate(tournament, players(8));
        matches.stream()
               .filter(m -> m.getBracketPosition()
                             .startsWith("W1"))
               .forEach(m -> {
                   assertNotNull(m.getPlayer1());
                   assertNotNull(m.getPlayer2());
               });
    }

    @Test
    void futureMatches_haveNullPlayers() {
        var matches = service.generate(tournament, players(8));
        matches.stream()
               .filter(m -> !m.getBracketPosition()
                              .startsWith("W1"))
               .forEach(m -> assertTrue(m.getPlayer1() == null || m.getPlayer2() == null,
                                        "Future match " + m.getBracketPosition() + " should have at least one null player"));
    }

    @Test
    void allMatchesAreScheduled() {
        var matches = service.generate(tournament, players(8));
        matches.forEach(m -> assertEquals(MatchStatus.SCHEDULED, m.getStatus()));
    }

    @Test
    void bracketPositionsAreUnique() {
        var matches = service.generate(tournament, players(8));
        long distinct = matches.stream()
                               .map(Match::getBracketPosition)
                               .distinct()
                               .count();
        assertEquals(14, distinct);
    }

    @Test
    void oddPlayerCount_throwsException() {
        assertThrows(Exception.class, () -> service.generate(tournament, players(6)));
    }

    @Test
    void gf1_hasCorrectPosition() {
        var matches = service.generate(tournament, players(8));
        assertTrue(matches.stream()
                          .anyMatch(m -> "GF1".equals(m.getBracketPosition())));
    }
}
