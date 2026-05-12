package be.technifutur.tournament.bl;

import be.technifutur.tournament.dal.MatchDAO;
import be.technifutur.tournament.dal.PlayerDao;
import be.technifutur.tournament.dal.RegistrationDAO;
import be.technifutur.tournament.dal.TournamentDAO;
import be.technifutur.tournament.dtl.TournamentBracketDataDto;
import be.technifutur.tournament.dl.entity.Player;
import be.technifutur.tournament.dl.entity.Registration;
import be.technifutur.tournament.dl.entity.Tournament;
import be.technifutur.tournament.dl.enums.RegistrationStatus;
import be.technifutur.tournament.dl.enums.TournamentStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class TournamentService {

    @Inject TournamentDAO tournamentDAO;
    @Inject RegistrationDAO registrationDAO;
    @Inject PlayerDao playerDAO;
    @Inject MatchDAO matchDAO;
    @Inject BracketGenerationService generationService;
    @Inject BracketService bracketService;
    @Inject GroupStageService groupStageService;

    public List<Tournament> findAll() {
        return tournamentDAO.findAll();
    }

    public Tournament findById(int id) {
        return tournamentDAO.findById(id)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));
    }

    public Tournament update(int id, String name, LocalDateTime startDate,
                             LocalDateTime registrationEndDate, Integer maxParticipants) {
        Tournament t = tournamentDAO.findById(id)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));
        if (maxParticipants != null && maxParticipants < 1)
            throw new BadRequestException("maxParticipants must be at least 1");
        if (registrationEndDate != null && startDate != null && registrationEndDate.isAfter(startDate))
            throw new BadRequestException("registrationEndDate must be before startDate");
        t.setName(name);
        t.setStartDate(startDate);
        t.setRegistrationEndDate(registrationEndDate);
        t.setMaxParticipants(maxParticipants);
        tournamentDAO.update(t);
        return t;
    }

    public void delete(int id) {
        tournamentDAO.findById(id)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));
        tournamentDAO.delete(id);
    }

    public Tournament create(String name, LocalDateTime startDate,
                             LocalDateTime registrationEndDate, Integer maxParticipants) {
        if (maxParticipants != null && maxParticipants < 1)
            throw new BadRequestException("maxParticipants must be at least 1");
        if (registrationEndDate != null && startDate != null && registrationEndDate.isAfter(startDate))
            throw new BadRequestException("registrationEndDate must be before startDate");
        Tournament t = Tournament.builder()
                .name(name).startDate(startDate)
                .registrationEndDate(registrationEndDate)
                .maxParticipants(maxParticipants)
                .status(TournamentStatus.DRAFT).build();
        tournamentDAO.save(t);
        return t;
    }

    public Registration register(int tournamentId, int playerId) {
        Tournament t = tournamentDAO.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));
        if (t.getStatus() != TournamentStatus.DRAFT && t.getStatus() != TournamentStatus.OPEN)
            throw new BadRequestException("Tournament has already started");
        if (t.getRegistrationEndDate() != null && LocalDateTime.now().isAfter(t.getRegistrationEndDate()))
            throw new BadRequestException("Registration period has ended");
        if (registrationDAO.existsByPlayerAndTournament(playerId, tournamentId))
            throw new WebApplicationException("Player already registered", 409);
        if (t.getMaxParticipants() != null
                && registrationDAO.countByTournament(tournamentId) >= t.getMaxParticipants())
            throw new BadRequestException("Tournament has reached its maximum number of participants");
        Player player = playerDAO.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        Registration reg = Registration.builder()
                .tournament(t).player(player).registrationStatus(RegistrationStatus.CONFIRMED).build();
        registrationDAO.save(reg);
        return reg;
    }

    public void unregister(int tournamentId, int playerId) {
        Tournament t = tournamentDAO.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));
        if (t.getStatus() != TournamentStatus.DRAFT && t.getStatus() != TournamentStatus.OPEN)
            throw new BadRequestException("Cannot unregister: tournament has already started");
        Registration reg = registrationDAO.findByPlayerAndTournament(playerId, tournamentId)
                .orElseThrow(() -> new NotFoundException("Registration not found"));
        registrationDAO.delete(reg.getId());
    }

    public List<Player> launchGroupStage(int tournamentId) {
        Tournament t = tournamentDAO.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));
        if (t.getStatus() != TournamentStatus.DRAFT)
            throw new BadRequestException("Tournament must be in DRAFT status");

        List<Registration> registrations = registrationDAO.findByTournamentWithStatus(tournamentId);
        int n = registrations.size();
        if (n % 4 != 0)
            throw new BadRequestException("Player count must be divisible by 4");
        int half = n / 2;
        if (half < 2 || (half & (half - 1)) != 0)
            throw new BadRequestException("N/2 must be a power of 2 (result must be 4, 8, 16...)");

        List<Player> players = registrations.stream().map(Registration::getPlayer).toList();
        List<Player> qualifiers = groupStageService.selectQualifiers(players);

        Set<Integer> qualifierIds = qualifiers.stream().map(Player::getId).collect(Collectors.toSet());
        registrationDAO.updateStatusForPlayers(tournamentId, qualifierIds, RegistrationStatus.QUALIFIED);

        t.setHasGroupStage(true);
        t.setStatus(TournamentStatus.GROUP_STAGE_COMPLETE);
        tournamentDAO.update(t);

        return qualifiers;
    }

    public TournamentBracketDataDto generateBracket(int tournamentId) {
        Tournament t = tournamentDAO.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));

        List<Registration> registrations;
        if (t.getStatus() == TournamentStatus.GROUP_STAGE_COMPLETE) {
            registrations = registrationDAO.findByTournamentAndStatus(tournamentId, RegistrationStatus.QUALIFIED);
        } else if (t.getStatus() == TournamentStatus.DRAFT) {
            registrations = registrationDAO.findByTournamentWithStatus(tournamentId);
        } else {
            throw new BadRequestException("Tournament must be in DRAFT or GROUP_STAGE_COMPLETE status");
        }

        int n = registrations.size();
        if (n < 4 || (n & (n - 1)) != 0)
            throw new BadRequestException("Player count must be a power of 2 (4, 8, 16...)");

        List<Player> players = registrations.stream().map(Registration::getPlayer).toList();
        generationService.generate(t, players).forEach(matchDAO::save);

        t.setStatus(TournamentStatus.IN_PROGRESS);
        tournamentDAO.update(t);

        return bracketService.buildBracketData(tournamentId);
    }

    public Tournament resetTournament(int tournamentId) {
        Tournament t = tournamentDAO.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));
        matchDAO.deleteByTournament(tournamentId);
        registrationDAO.resetAllStatusForTournament(tournamentId, RegistrationStatus.CONFIRMED);
        t.setHasGroupStage(false);
        t.setStatus(TournamentStatus.DRAFT);
        tournamentDAO.update(t);
        return t;
    }

    public Tournament updateStatus(int tournamentId, TournamentStatus status) {
        Tournament t = tournamentDAO.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));
        t.setStatus(status);
        tournamentDAO.update(t);
        return t;
    }
}