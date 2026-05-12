package be.technifutur.tournament.utils;

import lombok.Builder;

import java.util.regex.Pattern;

public class BracketStringUtil {

    @Builder
    public record BracketPosition(String prefix,
                                  int round,
                                  int match) {
    }

    public static BracketPosition extractBracketInfo(String str) {
        var m = Pattern.compile("(?<prefix>W|L|GF)(?<round>[0-9A-Fa-f])(?<match>\\d{0,5})$")
                       .matcher(str);
        if (m.matches()) {
            String match = m.group("match");
            return BracketPosition
                    .builder()
                    .prefix(m.group("prefix"))
                    .round(Integer.parseInt(m.group("round"), 16))
                    .match(match.isEmpty()? 0:Integer.parseInt(match))
                    .build();
        }
        return null;
    }
}
