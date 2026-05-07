package be.technifutur.tournament.bl;

import be.technifutur.tournament.dal.TournamentDAO;
import be.technifutur.tournament.dtl.TournamentBracketDataDto;
import be.technifutur.tournament.dl.entity.Match;
import be.technifutur.tournament.dl.entity.Player;
import be.technifutur.tournament.dl.entity.Tournament;
import be.technifutur.tournament.dl.enums.FinishType;
import be.technifutur.tournament.dl.enums.MatchStatus;
import be.technifutur.tournament.dl.enums.TournamentStatus;
import be.technifutur.tournament.utils.EMFProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@ApplicationScoped
public class TournamentSimulationService {

    @Inject TournamentDAO tournamentDAO;
    @Inject BracketRoutingService routingService;
    @Inject BracketService bracketService;
    @Inject EMFProvider emfProvider;

    private final Random random = new Random();

    public TournamentBracketDataDto simulateNextRound(int tournamentId) {
        Tournament t = tournamentDAO.findById(tournamentId)
            .orElseThrow(() -> new NotFoundException("Tournament not found"));
        if (t.getStatus() != TournamentStatus.IN_PROGRESS)
            throw new BadRequestException("Tournament must be in IN_PROGRESS status");

        boolean anyResolved = resolveReadyMatches(tournamentId);

        if (!anyResolved) {
            t.setStatus(TournamentStatus.FINISHED);
            tournamentDAO.update(t);
        }

        return bracketService.buildBracketData(tournamentId);
    }

    private boolean resolveReadyMatches(int tournamentId) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            em.getTransaction().begin();

            List<Match> ready = em.createQuery(
                "SELECT m FROM Match m " +
                "JOIN FETCH m.player1 LEFT JOIN FETCH m.player1.fighterMain " +
                "JOIN FETCH m.player2 LEFT JOIN FETCH m.player2.fighterMain " +
                "WHERE m.tournament.id = :tid AND m.status <> :finished " +
                "AND m.player1 IS NOT NULL AND m.player2 IS NOT NULL " +
                "ORDER BY m.roundNumber ASC NULLS LAST, m.id ASC",
                Match.class)
                .setParameter("tid", tournamentId)
                .setParameter("finished", MatchStatus.FINISHED)
                .getResultList();

            if (ready.isEmpty()) {
                em.getTransaction().commit();
                return false;
            }

            int totalPlayers = countQualifiedOrConfirmed(em, tournamentId);

            for (Match m : ready) {
                int[] scores = randomScores(m.getNumberRounds());
                Player winner = scores[0] > scores[1] ? m.getPlayer1() : m.getPlayer2();
                Player loser  = scores[0] > scores[1] ? m.getPlayer2() : m.getPlayer1();

                m.setPlayer1Score(scores[0]);
                m.setPlayer2Score(scores[1]);
                m.setStatus(MatchStatus.FINISHED);
                m.setFinishType(FinishType.KO);
                m.setFinishedAt(LocalDateTime.now());

                var routing = routingService.compute(m.getBracketPosition(), totalPlayers);
                if (routing.nextWinnerPosition() != null) {
                    placePlayer(em, tournamentId, routing.nextWinnerPosition(), winner);
                }
                if (routing.nextLoserPosition() != null) {
                    placePlayer(em, tournamentId, routing.nextLoserPosition(), loser);
                }
            }

            em.getTransaction().commit();
            return true;
        }
    }

    private int countQualifiedOrConfirmed(EntityManager em, int tournamentId) {
        Long qualified = em.createQuery(
            "SELECT COUNT(r) FROM Registration r " +
            "WHERE r.tournament.id = :tid AND r.registrationStatus = 'QUALIFIED'",
            Long.class)
            .setParameter("tid", tournamentId)
            .getSingleResult();
        if (qualified > 0) return qualified.intValue();
        Long confirmed = em.createQuery(
            "SELECT COUNT(r) FROM Registration r " +
            "WHERE r.tournament.id = :tid AND r.registrationStatus = 'CONFIRMED'",
            Long.class)
            .setParameter("tid", tournamentId)
            .getSingleResult();
        return confirmed.intValue();
    }

    private void placePlayer(EntityManager em, int tournamentId, String position, Player player) {
        List<Match> targets = em.createQuery(
            "SELECT m FROM Match m " +
            "WHERE m.tournament.id = :tid AND m.bracketPosition = :pos",
            Match.class)
            .setParameter("tid", tournamentId)
            .setParameter("pos", position)
            .getResultList();
        if (targets.isEmpty()) return;
        Match target = targets.get(0);
        if (target.getPlayer1() == null) target.setPlayer1(player);
        else if (target.getPlayer2() == null) target.setPlayer2(player);
    }

    private int[] randomScores(int numberRounds) {
        int winsNeeded = numberRounds / 2 + 1;
        int loserScore = random.nextInt(winsNeeded);
        return random.nextBoolean()
            ? new int[]{winsNeeded, loserScore}
            : new int[]{loserScore, winsNeeded};
    }
}
