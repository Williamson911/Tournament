package be.technifutur.tournament.bl;

import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static be.technifutur.tournament.bl.MatchConclusion.LOSER;
import static be.technifutur.tournament.bl.MatchConclusion.WINNER;
import static be.technifutur.tournament.bl.businessLayerUtil.provideMatchArguments;
import static org.junit.jupiter.api.Assertions.*;

class BracketRoutingServiceTest {

    // ── 8-player WB routing ──────────────────────────────────────
    static Stream<Arguments> provideRoundersNextBracketPosition() {
        return provideMatchArguments(8,Stream.of(
                Arguments.of("W11", WINNER, "W21"),
                Arguments.of("W12", WINNER, "W21"),
                Arguments.of("W13", WINNER, "W22"),
                Arguments.of("W11", LOSER, "L11"),
                Arguments.of("W12", LOSER, "L11"),
                Arguments.of("W13", LOSER, "L12"),

                Arguments.of("W21", WINNER, "W31"),
                Arguments.of("W21", LOSER, "L21"),
                Arguments.of("W22", LOSER, "L22"),
                Arguments.of("W31", WINNER, "GF1"),
                Arguments.of("W31", LOSER, "L41"),

                // ── 8-player LB routing ──────────────────────────────────────

                Arguments.of("L11", WINNER, "L21"),
                Arguments.of("L12", WINNER, "L22"),
                Arguments.of("L21", WINNER, "L31"),
                Arguments.of("L22", WINNER, "L31"),
                Arguments.of("L31", WINNER, "L41"),
                Arguments.of("L41", WINNER, "GF1"),

                // ── LB elimination ────────────────────────────────────────────

                Arguments.of("L11", LOSER, null),
                Arguments.of("L41", LOSER, null),

                // ── GF routing ───────────────────────────────────────────────

                Arguments.of("GF1", WINNER, null),
                Arguments.of("GF1", LOSER, null)
        ));
    }

    @ParameterizedTest(name = "The {0} in {1}''s next bracket should be {2}")
    @MethodSource("provideRoundersNextBracketPosition")
    void roundersInTournamentMoveToAccordingPosition(MatchConclusion ignored, String ignored2, Object bracketPosition,String expected) {
        assertEquals(expected,bracketPosition);
    }


    // ── 256-player WB routing ──────────────────────────────────────
    static Stream<Arguments> provideRoundersNextBracketPositionBigTournament() {
        return provideMatchArguments(2^8,Stream.of(
                Arguments.of("L11", WINNER, "L21"),
                Arguments.of("L12", WINNER, "L22"),
                Arguments.of("L13", WINNER, "L23"),
                Arguments.of("L14", WINNER, "L24"),

                Arguments.of("L21", WINNER, "L31"),
                Arguments.of("L22", WINNER, "L31"),
                Arguments.of("L23", WINNER, "L32"),
                Arguments.of("L24", WINNER, "L32"),

                Arguments.of("L31", WINNER, "L41"),
                Arguments.of("L32", WINNER, "L42"),

                Arguments.of("L41", WINNER, "L51"),
                Arguments.of("L42", WINNER, "L51"),

                Arguments.of("L51", WINNER, "L61"),

                //Arguments.of("L61", WINNER, "GF1"),

                Arguments.of("W11", LOSER, "L01"),
                //Arguments.of("WB256", LOSER, "L11"),
                //Arguments.of("W13", LOSER, "L12"),
                //Arguments.of("W14", LOSER, "L12"),

                Arguments.of("W21", LOSER, "L21"),
                Arguments.of("W22", LOSER, "L22"),

                Arguments.of("W31", LOSER, "L41"),

                Arguments.of("W41", LOSER, "L61"),

                Arguments.of("LA1", WINNER, "LB1"),
                Arguments.of("LA2", WINNER, "LB1"),
                Arguments.of("LA3", WINNER, "LB2"),
                Arguments.of("LA4", WINNER, "LB2"),

                Arguments.of("LB128", WINNER, "LC128"),
                Arguments.of("LB2", WINNER, "LC2"),

                Arguments.of("LC1", WINNER, "LD1"),
                Arguments.of("LC2", WINNER, "LD1"),

                Arguments.of("LD1", WINNER, "LE1"),

                Arguments.of("LE1", WINNER, "LF1")

                //Arguments.of("LF1", WINNER, "GF1")
        ));
    }

    @ParameterizedTest(name = "The {0} in {1}''s next bracket should be {2}")
    @MethodSource("provideRoundersNextBracketPositionBigTournament")
    void roundersInBigTournamentMoveToAccordingPosition(MatchConclusion ignored, String ignored2, Object bracketPosition,String expected) {
        assertEquals(expected,bracketPosition);
    }
}
