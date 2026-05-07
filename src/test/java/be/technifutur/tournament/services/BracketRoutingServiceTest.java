package be.technifutur.tournament.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BracketRoutingServiceTest {

    private BracketRoutingService service;

    @BeforeEach
    void setUp() { service = new BracketRoutingService(); }

    // ── 8-player WB routing ──────────────────────────────────────
    @Test void wbR1_M1_winner_goesToW21() {
        var r = service.compute("W11", 8);
        assertEquals("W21", r.nextWinnerPosition());
    }
    @Test void wbR1_M2_winner_goesToW21() {
        var r = service.compute("W12", 8);
        assertEquals("W21", r.nextWinnerPosition());
    }
    @Test void wbR1_M3_winner_goesToW22() {
        var r = service.compute("W13", 8);
        assertEquals("W22", r.nextWinnerPosition());
    }
    @Test void wbR1_M1_loser_goesToL11() {
        var r = service.compute("W11", 8);
        assertEquals("L11", r.nextLoserPosition());
    }
    @Test void wbR1_M2_loser_goesToL11() {
        var r = service.compute("W12", 8);
        assertEquals("L11", r.nextLoserPosition());
    }
    @Test void wbR1_M3_loser_goesToL12() {
        var r = service.compute("W13", 8);
        assertEquals("L12", r.nextLoserPosition());
    }
    @Test void wbR2_M1_winner_goesToW31() {
        var r = service.compute("W21", 8);
        assertEquals("W31", r.nextWinnerPosition());
    }
    @Test void wbR2_M1_loser_goesToL21() {
        var r = service.compute("W21", 8);
        assertEquals("L21", r.nextLoserPosition());
    }
    @Test void wbR2_M2_loser_goesToL22() {
        var r = service.compute("W22", 8);
        assertEquals("L22", r.nextLoserPosition());
    }
    @Test void wbFinal_winner_goesToGF1() {
        var r = service.compute("W31", 8);
        assertEquals("GF1", r.nextWinnerPosition());
    }
    @Test void wbFinal_loser_goesToL41() {
        var r = service.compute("W31", 8);
        assertEquals("L41", r.nextLoserPosition());
    }

    // ── 8-player LB routing ──────────────────────────────────────
    @Test void lbR1_M1_winner_goesToL21() {
        var r = service.compute("L11", 8);
        assertEquals("L21", r.nextWinnerPosition());
    }
    @Test void lbR1_M1_loser_isEliminated() {
        var r = service.compute("L11", 8);
        assertNull(r.nextLoserPosition());
    }
    @Test void lbR1_M2_winner_goesToL22() {
        var r = service.compute("L12", 8);
        assertEquals("L22", r.nextWinnerPosition());
    }
    @Test void lbR2_M1_winner_goesToL31() {
        var r = service.compute("L21", 8);
        assertEquals("L31", r.nextWinnerPosition());
    }
    @Test void lbR2_M2_winner_goesToL31() {
        var r = service.compute("L22", 8);
        assertEquals("L31", r.nextWinnerPosition());
    }
    @Test void lbR3_M1_winner_goesToL41() {
        var r = service.compute("L31", 8);
        assertEquals("L41", r.nextWinnerPosition());
    }
    @Test void lbFinal_winner_goesToGF1() {
        var r = service.compute("L41", 8);
        assertEquals("GF1", r.nextWinnerPosition());
    }
    @Test void lbFinal_loser_isEliminated() {
        var r = service.compute("L41", 8);
        assertNull(r.nextLoserPosition());
    }

    // ── GF routing ───────────────────────────────────────────────
    @Test void gf_winner_hasNoNextPosition() {
        var r = service.compute("GF1", 8);
        assertNull(r.nextWinnerPosition());
    }
    @Test void gf_loser_isEliminated() {
        var r = service.compute("GF1", 8);
        assertNull(r.nextLoserPosition());
    }
}
