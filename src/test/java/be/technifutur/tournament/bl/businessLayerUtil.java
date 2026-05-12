package be.technifutur.tournament.bl;

import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static be.technifutur.tournament.bl.MatchConclusion.WINNER;

public class businessLayerUtil {
    public static Stream<Arguments> provideMatchArguments(int playerPoolSize, Stream<Arguments> evolutions) {
        var service = new BracketRoutingService();
        List<Arguments> args = new ArrayList<>();

        evolutions.forEach(arguments -> {
            Object[] values = arguments.get();

            String bracket = (String) values[0];
            MatchConclusion conclusion = (MatchConclusion) values[1];
            String expectBracket = (String) values[2];

            String nextBracket = conclusion == WINNER ?
                    service.compute(bracket, playerPoolSize)
                           .nextWinnerPosition() :
                    service.compute(bracket, playerPoolSize)
                           .nextLoserPosition();

            args.add(Arguments.of(conclusion, bracket, nextBracket, expectBracket));
        });
        return args.stream();
    }
}
