package be.technifutur.tournament.bl;

import be.technifutur.tournament.dl.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class GroupStageServiceTest {

    private GroupStageService service;

    @BeforeEach
    void setUp() {
        service = new GroupStageService();
    }

    private List<Player> players(int count) {
        return IntStream.rangeClosed(1, count)
            .mapToObj(i -> Player.builder().username("p" + i).build())
            .toList();
    }

    @Test
    void selectQualifiers_32_returns_16() {
        assertEquals(16, service.selectQualifiers(players(32)).size());
    }

    @Test
    void selectQualifiers_8_returns_4() {
        assertEquals(4, service.selectQualifiers(players(8)).size());
    }

    @Test
    void selectQualifiers_all_from_input() {
        List<Player> input = players(16);
        List<Player> qualifiers = service.selectQualifiers(input);
        assertTrue(input.containsAll(qualifiers));
    }

    @Test
    void selectQualifiers_no_duplicates() {
        List<Player> qualifiers = service.selectQualifiers(players(32));
        long unique = qualifiers.stream().distinct().count();
        assertEquals(16, unique);
    }
}
