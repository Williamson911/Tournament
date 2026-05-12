package be.technifutur.tournament.api;

import be.technifutur.tournament.dl.enums.TournamentActionenum;
import be.technifutur.tournament.dtl.tournament.TournamentActionRequestDTO;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TournamentResourceTest {
    @Test
    void applyYieldsBadRequestOnUnhandledCase() {
        TournamentResource resource = new TournamentResource();

        var data = resource.apply(new TournamentActionRequestDTO(TournamentActionenum.BOOM,null));
        Assertions.assertEquals("Unsupported operation", data.getEntity());
    }
}