package be.technifutur.tournament.dl.enums;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static be.technifutur.tournament.dl.enums.TournamentActionenum.LAUNCH_GROUP_STAGE;
import static org.junit.jupiter.api.Assertions.*;

class TournamentActionenumTest {

    @Test
    void fromStringGetsTheEnumFromActualKeys() {
        Assertions.assertEquals(LAUNCH_GROUP_STAGE,TournamentActionenum.fromString("launch-group-stage"));
    }
    @Test
    void fromStringGetsTheEnumFromItsConstantNameUpperCased() {
        Assertions.assertEquals(LAUNCH_GROUP_STAGE,TournamentActionenum.fromString("LAUNCH_GROUP_STAGE"));
    }
}