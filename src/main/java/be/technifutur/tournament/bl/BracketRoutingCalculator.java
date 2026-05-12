package be.technifutur.tournament.bl;

import be.technifutur.tournament.utils.HexConverter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BracketRoutingCalculator {

    private final int round;
    private final int match;
    private final int totalPlayers;

    public BracketRoutingService.RoutingResult routingResultOfLosers() {

        if (round == getLosersBracketRounds()) {
            return new BracketRoutingService.RoutingResult("GF1", null);
        }

        int nextRound = round + 1;

        String nextWinner = (round % 2 == 0)
                ? loser(nextRound, (match + 1) / 2)
                : loser(nextRound, match);

        return new BracketRoutingService.RoutingResult(nextWinner, null);
    }

    public BracketRoutingService.RoutingResult routingResultOfWinners() {

        if (round == getWinnerBracketRounds()) {

            return new BracketRoutingService.RoutingResult(
                    "GF1",
                    loser(getLosersBracketRounds(), 1)
            );
        }

        int nextRound = round + 1;

        String nextWinner = winner(nextRound, (match + 1) / 2);

        String nextLoser = (round == 1)
                ? loser(1, (match + 1) / 2)
                : loser(round * 2 - 2, match);

        return new BracketRoutingService.RoutingResult(
                nextWinner,
                nextLoser
        );
    }

    private String winner(int round, int match) {
        //validateRound(round);
        return "W" + HexConverter.integerToHex(round) + match;
    }

    private String loser(int round, int match) {
        //validateRound(round);
        return "L" + HexConverter.integerToHex(round) + match;
    }

    private void validateRound(int round) {
        if (round < 0 || round > 15) {
            throw new IllegalArgumentException(
                    "Single hex digit supports rounds 0-F only. Round=" + round
            );
        }
    }

    private int getWinnerBracketRounds() {
        return Integer.numberOfTrailingZeros(totalPlayers);
    }

    private int getLosersBracketRounds() {
        return 2 * (getWinnerBracketRounds() - 1);
    }
}
