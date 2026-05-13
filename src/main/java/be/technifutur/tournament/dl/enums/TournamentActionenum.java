package be.technifutur.tournament.dl.enums;

import lombok.Getter;

@Getter
public enum TournamentActionenum {
    CREATE("create","creates a tournament"),
    REGISTER("registrations", "create registrations"),
    GENERATE_BRACKET("generate-bracket", "generate a bracket"),
    LAUNCH_GROUP_STAGE("launch-group-stage", "Run group stage and select qualifiers"),
    SIMULATE_NEXT_ROUND("simulate-next-round", "Auto-simulate one round of ready matches"),
    APPLY_BRAWL("apply-brawl","defines how many match play and optionally who wins"),
    APPLY_DUEL("apply-duel","decide for one match who wins"),
    RESET("reset", "Reset tournament to DRAFT (delete matches, restore registrations)"),
    SIMULATE_ONE_MATCH("simulate-one-match","Auto-simulate a single ready match" ),
    BOOM("boom","this should make things explode !");

    private final String key;
    private final String description;

    TournamentActionenum(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public static TournamentActionenum fromString(String key) {
        for (TournamentActionenum b : TournamentActionenum.values()) {
            if (b.key.equalsIgnoreCase(key)) {
                return b;
            }
        }
        return TournamentActionenum.valueOf(key);
    }
}