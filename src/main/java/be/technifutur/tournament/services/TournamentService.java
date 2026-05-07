package be.technifutur.tournament.services;

import be.technifutur.tournament.daos.MatchDAO;
import be.technifutur.tournament.daos.PlayerDao;
import be.technifutur.tournament.daos.RegistrationDAO;
import be.technifutur.tournament.daos.TournamentDAO;
import be.technifutur.tournament.dtos.TournamentBracketDataDto;
import be.technifutur.tournament.entities.Player;
import be.technifutur.tournament.entities.Registration;
import be.technifutur.tournament.entities.Tournament;
import be.technifutur.tournament.enums.RegistrationStatus;
import be.technifutur.tournament.enums.TournamentStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class TournamentService {

    @Inject TournamentDAO tournamentDAO;
    @Inject RegistrationDAO registrationDAO;
    @Inject PlayerDao playerDAO;
    @Inject MatchDAO matchDAO;
    @Inject BracketGenerationService generationService;
    @Inject BracketService bracketService;

    public Tournament create(String name, LocalDateTime startDate) {
        Tournament t = Tournament.builder()
            .name(name).startDate(startDate).status(TournamentStatus.DRAFT).build();
        tournamentDAO.save(t);
        return t;
    }

    public Registration register(int tournamentId, int playerId) {
        Tournament t = tournamentDAO.findById(tournamentId)
            .orElseThrow(() -> new NotFoundException("Tournament not found"));
        if (t.getStatus() != TournamentStatus.DRAFT && t.getStatus() != TournamentStatus.OPEN)
            throw new BadRequestException("Registration is closed for this tournament");
        if (registrationDAO.existsByPlayerAndTournament(playerId, tournamentId))
            throw new WebApplicationException("Player already registered", 409);
        Player player = playerDAO.findById(playerId)
            .orElseThrow(() -> new NotFoundException("Player not found"));
        Registration reg = Registration.builder()
            .tournament(t).player(player).registrationStatus(RegistrationStatus.CONFIRMED).build();
        registrationDAO.save(reg);
        return reg;
    }

    public void unregister(int tournamentId, int playerId) {
        Registration reg = registrationDAO.findByPlayerAndTournament(playerId, tournamentId)
            .orElseThrow(() -> new NotFoundException("Registration not found"));
        registrationDAO.delete(reg.getId());
    }

    public TournamentBracketDataDto generateBracket(int tournamentId) {
        Tournament t = tournamentDAO.findById(tournamentId)
            .orElseThrow(() -> new NotFoundException("Tournament not found"));
        List<Registration> registrations = registrationDAO.findByTournamentWithStatus(tournamentId);
        int n = registrations.size();
        if (n < 4 || (n & (n - 1)) != 0)
            throw new BadRequestException("Player count must be a power of 2 (4, 8, 16...)");

        List<Player> players = registrations.stream().map(Registration::getPlayer).toList();
        generationService.generate(t, players).forEach(matchDAO::save);

        t.setStatus(TournamentStatus.IN_PROGRESS);
        tournamentDAO.update(t);

        return bracketService.buildBracketData(tournamentId);
    }

    public Tournament updateStatus(int tournamentId, TournamentStatus status) {
        Tournament t = tournamentDAO.findById(tournamentId)
            .orElseThrow(() -> new NotFoundException("Tournament not found"));
        t.setStatus(status);
        tournamentDAO.update(t);
        return t;
    }
}
