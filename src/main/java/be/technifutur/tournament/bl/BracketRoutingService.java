package be.technifutur.tournament.bl;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BracketRoutingService {

    public record RoutingResult(String nextWinnerPosition, String nextLoserPosition) {}

    /**
     * Computes next bracket positions from a given match position.
     * totalPlayers must be a power of 2 (4, 8, 16...).
     */
    public RoutingResult compute(String bracketPosition, int totalPlayers) {
        int wbRounds = (int)(Math.log(totalPlayers) / Math.log(2));
        int lbRounds = 2 * (wbRounds - 1);

        if (bracketPosition.startsWith("GF")) {
            return new RoutingResult(null, null);
        }

        if (bracketPosition.startsWith("W")) {
            int round = Character.getNumericValue(bracketPosition.charAt(1));
            int match = Character.getNumericValue(bracketPosition.charAt(2));

            if (round == wbRounds) {
                return new RoutingResult("GF1", "L" + lbRounds + "1");
            }

            String nextWinner = "W" + (round + 1) + (int)Math.ceil(match / 2.0);
            String nextLoser = (round == 1)
                ? "L1" + (int)Math.ceil(match / 2.0)
                : "L" + (round * 2 - 2) + match;
            return new RoutingResult(nextWinner, nextLoser);
        }

        if (bracketPosition.startsWith("L")) {
            int round = Character.getNumericValue(bracketPosition.charAt(1));
            int match = Character.getNumericValue(bracketPosition.charAt(2));

            if (round == lbRounds) {
                return new RoutingResult("GF1", null);
            }

            String nextWinner = (round % 2 == 1)
                ? "L" + (round + 1) + match
                : "L" + (round + 1) + (int)Math.ceil(match / 2.0);
            return new RoutingResult(nextWinner, null);
        }

        throw new IllegalArgumentException("Unknown bracket position: " + bracketPosition);
    }
}
