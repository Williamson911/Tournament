package be.technifutur.tournament.bl;

import be.technifutur.tournament.utils.BracketStringUtil;
import be.technifutur.tournament.utils.HexConverter;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BracketRoutingService {

    public record RoutingResult(String nextWinnerPosition,
                                String nextLoserPosition) {
    }

    /**
     * Computes next bracket positions from a given match position.
     * totalPlayers must be a power of 2 (4, 8, 16...).
     */
    public RoutingResult compute(String bracketPosition, int totalPlayers) {

        var bracket = BracketStringUtil.extractBracketInfo(bracketPosition);
        if (bracket != null) {
            if ("GF".equals(bracket.prefix())) {
                return new RoutingResult(null, null);
            }

            int round = bracket.round();
            int match = bracket.match();

            if (bracket.prefix()
                       .startsWith("W")) {
                return new BracketRoutingCalculator(round,match,totalPlayers).routingResultOfWinners();
            } else if (bracket.prefix()
                              .startsWith("L")) {

                return new BracketRoutingCalculator(round,match,totalPlayers).routingResultOfLosers();
            }
        }


        throw new IllegalArgumentException("Unknown bracket position: " + bracketPosition);
    }
}
