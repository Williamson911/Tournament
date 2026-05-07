package be.technifutur.tournament.dtos;


public record MatchParticipantDto(
    int playerId,
    String playerName,
    String fighterName,
    String fighterImageUrl,
    int score,
    boolean isWinner,
    boolean isEliminated
) {}