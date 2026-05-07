package be.technifutur.tournament.dtl;


public record MatchParticipantDto(
    int playerId,
    String playerName,
    String fighterName,
    String fighterImageUrl,
    int score,
    boolean isWinner,
    boolean isEliminated
) {}