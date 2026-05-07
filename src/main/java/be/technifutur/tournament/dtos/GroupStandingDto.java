package be.technifutur.tournament.dtos;

public record GroupStandingDto(
    int rank,
    TournamentParticipantDto participant,
    int wins,
    int losses,
    int points,
    boolean qualified
) {}
