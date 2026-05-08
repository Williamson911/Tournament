package be.technifutur.tournament.dtl;

import java.time.LocalDateTime;

public record CreateTournamentDto(
        String name,
        LocalDateTime startDate,
        LocalDateTime registrationEndDate,
        Integer maxParticipants
) {}
