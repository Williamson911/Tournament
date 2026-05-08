package be.technifutur.tournament.dtl.tournament;

import be.technifutur.tournament.dl.entity.Tournament;
import be.technifutur.tournament.dl.enums.TournamentStatus;

import java.time.LocalDateTime;

public record TournamentDTO(
        Integer id,
        String name,
        TournamentStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
    public static TournamentDTO toDTO(Tournament t){
        return new TournamentDTO(
                t.getId(),
                t.getName(),
                t.getStatus(),
                t.getStartDate(),
                t.getEndDate()

        );
    }
}
