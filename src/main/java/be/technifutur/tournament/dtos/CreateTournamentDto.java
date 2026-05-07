package be.technifutur.tournament.dtos;

import java.time.LocalDateTime;

public record CreateTournamentDto(String name, LocalDateTime startDate) {}
