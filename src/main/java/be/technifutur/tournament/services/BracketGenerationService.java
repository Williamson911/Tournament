package be.technifutur.tournament.services;

import be.technifutur.tournament.entities.Match;
import be.technifutur.tournament.entities.Player;
import be.technifutur.tournament.entities.Tournament;
import be.technifutur.tournament.enums.BracketStage;
import be.technifutur.tournament.enums.MatchStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class BracketGenerationService {

    public List<Match> generate(Tournament tournament, List<Player> players) {
        int n = players.size();
        if (n < 4 || (n & (n - 1)) != 0)
            throw new BadRequestException("Player count must be a power of 2 (4, 8, 16...)");

        int wbRounds = (int)(Math.log(n) / Math.log(2));
        int lbRounds = 2 * (wbRounds - 1);

        List<Player> shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled);

        List<Match> matches = new ArrayList<>();

        // Winners Bracket
        for (int r = 1; r <= wbRounds; r++) {
            int matchCount = n / (int)Math.pow(2, r);
            for (int m = 1; m <= matchCount; m++) {
                Player p1 = null, p2 = null;
                if (r == 1) {
                    p1 = shuffled.get((m - 1) * 2);
                    p2 = shuffled.get((m - 1) * 2 + 1);
                }
                matches.add(buildMatch(tournament, "W" + r + m, p1, p2, BracketStage.WINNERS_BRACKET, r));
            }
        }

        // Losers Bracket
        for (int r = 1; r <= lbRounds; r++) {
            int matchCount = n / (int)Math.pow(2, (int)Math.ceil(r / 2.0) + 1);
            for (int m = 1; m <= matchCount; m++) {
                matches.add(buildMatch(tournament, "L" + r + m, null, null, BracketStage.LOSERS_BRACKET, r));
            }
        }

        // Grand Final (best of 5)
        matches.add(buildMatch(tournament, "GF1", null, null, BracketStage.GRAND_FINAL, 1, 5));

        return matches;
    }

    private Match buildMatch(Tournament tournament, String pos, Player p1, Player p2,
                              BracketStage stage, int round) {
        return buildMatch(tournament, pos, p1, p2, stage, round, 3);
    }

    private Match buildMatch(Tournament tournament, String pos, Player p1, Player p2,
                              BracketStage stage, int round, int numberRounds) {
        return Match.builder()
            .tournament(tournament)
            .bracketPosition(pos)
            .bracketStage(stage)
            .roundNumber(round)
            .player1(p1).player2(p2)
            .player1Score(null).player2Score(null)
            .numberRounds(numberRounds)
            .status(MatchStatus.SCHEDULED)
            .build();
    }
}
