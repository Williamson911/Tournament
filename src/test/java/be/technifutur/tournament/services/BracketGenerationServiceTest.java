package be.technifutur.tournament.services;

import be.technifutur.tournament.entities.Match;
import be.technifutur.tournament.entities.Player;
import be.technifutur.tournament.entities.Tournament;
import be.technifutur.tournament.enums.BracketStage;
import be.technifutur.tournament.enums.MatchStatus;
import be.technifutur.tournament.enums.TournamentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

class BracketGenerationServiceTest {

    private BracketGenerationService service;
    private Tournament tournament;

    @BeforeEach
    void setUp() {
        service = new BracketGenerationService();
        tournament = Tournament.builder().name("Test").status(TournamentStatus.DRAFT).build();
    }

    private List<Player> players(int count) {
        return IntStream.rangeClosed(1, count)
            .mapToObj(i -> Player.builder().username("p" + i).build())
            .toList();
    }

    @Test void eightPlayers_generates14Matches() {
        var matches = service.generate(tournament, players(8));
        assertEquals(14, matches.size());
    }
    @Test void fourPlayers_generates6Matches() {
        var matches = service.generate(tournament, players(4));
        assertEquals(6, matches.size());
    }
    @Test void eightPlayers_hasCorrectWBMatchCount() {
        var matches = service.generate(tournament, players(8));
        long wb = matches.stream().filter(m -> m.getBracketStage() == BracketStage.WINNERS_BRACKET).count();
        assertEquals(7, wb); // 4+2+1
    }
    @Test void eightPlayers_hasCorrectLBMatchCount() {
        var matches = service.generate(tournament, players(8));
        long lb = matches.stream().filter(m -> m.getBracketStage() == BracketStage.LOSERS_BRACKET).count();
        assertEquals(6, lb); // 2+2+1+1
    }
    @Test void eightPlayers_hasOneGF() {
        var matches = service.generate(tournament, players(8));
        long gf = matches.stream().filter(m -> m.getBracketStage() == BracketStage.GRAND_FINAL).count();
        assertEquals(1, gf);
    }
    @Test void wbR1Matches_havePlayersAssigned() {
        var matches = service.generate(tournament, players(8));
        matches.stream()
            .filter(m -> m.getBracketPosition().startsWith("W1"))
            .forEach(m -> {
                assertNotNull(m.getPlayer1());
                assertNotNull(m.getPlayer2());
            });
    }
    @Test void futureMatches_haveNullPlayers() {
        var matches = service.generate(tournament, players(8));
        matches.stream()
            .filter(m -> !m.getBracketPosition().startsWith("W1"))
            .forEach(m -> assertTrue(m.getPlayer1() == null || m.getPlayer2() == null,
                "Future match " + m.getBracketPosition() + " should have at least one null player"));
    }
    @Test void allMatchesAreScheduled() {
        var matches = service.generate(tournament, players(8));
        matches.forEach(m -> assertEquals(MatchStatus.SCHEDULED, m.getStatus()));
    }
    @Test void bracketPositionsAreUnique() {
        var matches = service.generate(tournament, players(8));
        long distinct = matches.stream().map(Match::getBracketPosition).distinct().count();
        assertEquals(14, distinct);
    }
    @Test void oddPlayerCount_throwsException() {
        assertThrows(Exception.class, () -> service.generate(tournament, players(6)));
    }
    @Test void gf1_hasCorrectPosition() {
        var matches = service.generate(tournament, players(8));
        assertTrue(matches.stream().anyMatch(m -> "GF1".equals(m.getBracketPosition())));
    }
}
