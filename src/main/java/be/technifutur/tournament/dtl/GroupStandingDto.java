package be.technifutur.tournament.dtl;

public record GroupStandingDto(
    int rank,
    TournamentParticipantDto participant,
    int wins,
    int losses,
    int points,
    boolean qualified
) {}
