package be.technifutur.tournament.services;

import be.technifutur.tournament.daos.MatchDAO;
import be.technifutur.tournament.daos.TournamentDAO;
import be.technifutur.tournament.dtos.TournamentBracketDataDto;
import be.technifutur.tournament.entities.Match;
import be.technifutur.tournament.entities.Tournament;
import be.technifutur.tournament.enums.FinishType;
import be.technifutur.tournament.enums.MatchStatus;
import be.technifutur.tournament.enums.TournamentStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.Random;

@ApplicationScoped
public class TournamentSimulationService {

    @Inject TournamentDAO tournamentDAO;
    @Inject MatchDAO matchDAO;
    @Inject MatchResultService matchResultService;
    @Inject BracketService bracketService;

    private final Random random = new Random();

    public TournamentBracketDataDto simulateNextRound(int tournamentId) {
        Tournament t = tournamentDAO.findById(tournamentId)
            .orElseThrow(() -> new NotFoundException("Tournament not found"));
        if (t.getStatus() != TournamentStatus.IN_PROGRESS)
            throw new BadRequestException("Tournament must be in IN_PROGRESS status");

        List<Match> matches = matchDAO.findByTournamentWithPlayers(tournamentId);
        boolean anyReady = false;
        for (Match m : matches) {
            if (m.getStatus() != MatchStatus.FINISHED
                && m.getPlayer1() != null && m.getPlayer2() != null) {
                int[] scores = randomScores();
                matchResultService.recordResult(m.getId(), scores[0], scores[1], FinishType.KO);
                anyReady = true;
            }
        }

        if (!anyReady) {
            t.setStatus(TournamentStatus.FINISHED);
            tournamentDAO.update(t);
        }

        return bracketService.buildBracketData(tournamentId);
    }

    private int[] randomScores() {
        int loserScore = random.nextInt(2);
        return random.nextBoolean()
            ? new int[]{2, loserScore}
            : new int[]{loserScore, 2};
    }
}
