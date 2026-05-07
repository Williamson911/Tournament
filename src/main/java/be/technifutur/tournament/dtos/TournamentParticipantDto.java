package be.technifutur.tournament.dtos;

public record TournamentParticipantDto(
    int playerId,
    String playerName,
    String fighterName,
    String fighterImageUrl
) {}
