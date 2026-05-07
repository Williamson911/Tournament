package be.technifutur.tournament.dtos;

import be.technifutur.tournament.enums.TournamentStatus;

public record UpdateStatusDto(TournamentStatus status) {}
