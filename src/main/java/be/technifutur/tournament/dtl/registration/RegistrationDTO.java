package be.technifutur.tournament.dtl.registration;

import be.technifutur.tournament.dtl.player.PlayerDTO;
import be.technifutur.tournament.dtl.tournament.TournamentDTO;
import be.technifutur.tournament.dl.entities.Registration;
import be.technifutur.tournament.dl.enums.RegistrationStatus;

import java.time.LocalDateTime;

public record RegistrationDTO(
        Integer id,
        RegistrationStatus status,
        LocalDateTime registeredDate,
        TournamentDTO tournament,
        PlayerDTO player

) {
    public static RegistrationDTO toDTO(Registration r){
        return new RegistrationDTO(
                r.getId(),
                r.getRegistrationStatus(),
                r.getRegisteredAt(),
                r.getTournament() != null ? TournamentDTO.toDTO(r.getTournament()): null,
                r.getPlayer() != null ? PlayerDTO.toDTO(r.getPlayer()): null
        );
    }
}
