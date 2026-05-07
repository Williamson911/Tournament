package be.technifutur.tournament.services;

import be.technifutur.tournament.daos.MatchDAO;
import be.technifutur.tournament.daos.RegistrationDAO;
import be.technifutur.tournament.dtos.TournamentBracketDataDto;
import be.technifutur.tournament.entities.Match;
import be.technifutur.tournament.entities.Player;
import be.technifutur.tournament.enums.FinishType;
import be.technifutur.tournament.enums.MatchStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDateTime;

@ApplicationScoped
public class MatchResultService {

    @Inject MatchDAO matchDAO;
    @Inject RegistrationDAO registrationDAO;
    @Inject BracketRoutingService routingService;
    @Inject BracketService bracketService;

    public TournamentBracketDataDto recordResult(int matchId, int p1Score, int p2Score, FinishType finishType) {
        Match match = matchDAO.findById(matchId)
            .orElseThrow(() -> new NotFoundException("Match not found"));

        if (match.getStatus() == MatchStatus.FINISHED)
            throw new WebApplicationException("Match already finished", 409);
        if (match.getPlayer1() == null || match.getPlayer2() == null)
            throw new BadRequestException("Match is not ready — players not yet assigned");
        if (p1Score == p2Score)
            throw new BadRequestException("Tie scores are not allowed");
        if (match.getBracketPosition() == null)
            throw new BadRequestException("Match has no bracket position assigned");

        match.setPlayer1Score(p1Score);
        match.setPlayer2Score(p2Score);
        match.setFinishType(finishType);
        match.setStatus(MatchStatus.FINISHED);
        match.setFinishedAt(LocalDateTime.now());
        matchDAO.update(match);

        Player winner = p1Score > p2Score ? match.getPlayer1() : match.getPlayer2();
        Player loser  = p1Score > p2Score ? match.getPlayer2() : match.getPlayer1();

        int tournamentId = match.getTournament().getId();
        int totalPlayers = registrationDAO.findByTournamentWithStatus(tournamentId).size();

        var routing = routingService.compute(match.getBracketPosition(), totalPlayers);

        if (routing.nextWinnerPosition() != null)
            placePlayer(tournamentId, routing.nextWinnerPosition(), winner);
        if (routing.nextLoserPosition() != null)
            placePlayer(tournamentId, routing.nextLoserPosition(), loser);

        return bracketService.buildBracketData(tournamentId);
    }

    private void placePlayer(int tournamentId, String position, Player player) {
        Match target = matchDAO.findByTournamentAndBracketPosition(tournamentId, position)
            .orElseThrow(() -> new NotFoundException("Target match at " + position + " not found"));
        if (target.getPlayer1() == null) {
            target.setPlayer1(player);
        } else if (target.getPlayer2() == null) {
            target.setPlayer2(player);
        } else {
            throw new WebApplicationException("Target match at " + position + " already has both players assigned", 409);
        }
        matchDAO.update(target);
    }
}
