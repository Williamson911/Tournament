package be.technifutur.tournament.dtl;

public record TournamentParticipantDto(
    int playerId,
    String playerName,
    String fighterName,
    String fighterImageUrl
) {}
