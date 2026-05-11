package be.technifutur.tournament.bl;

import lombok.Getter;

@Getter
public enum MatchConclusion {
    WINNER("winner"),
    LOSER("loser");

    final String value;

    MatchConclusion(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
