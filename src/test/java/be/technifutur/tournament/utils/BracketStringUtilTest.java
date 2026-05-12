package be.technifutur.tournament.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BracketStringUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {"W11", "W99454", "W04687", "L11", "LA454", "LF32768", "GF1"})
    void extractBracket(String str) {
        Assertions.assertNotNull(BracketStringUtil.extractBracketInfo(str));
    }
}