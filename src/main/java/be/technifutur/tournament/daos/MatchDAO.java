package be.technifutur.tournament.daos;

import be.technifutur.tournament.entities.Match;

import java.util.List;
import java.util.Optional;

public class MatchDAO extends CrudDao<Match, Long> {

    public List<Match> findByTournamentAndRound(Long tournamentId, int round) {
        try (var em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT m FROM Match m WHERE m.tournament.id = :tid AND m.roundNumber = :round",
                            Match.class)
                    .setParameter("tid", tournamentId)
                    .setParameter("round", round)
                    .getResultList();
        }
    }

    public Optional<Match> findNextMatchForPlayer(Long playerId, Long tournamentId) {
        try (var em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT m FROM Match m WHERE m.tournament.id = :tid AND (m.player1.id = :pid OR m.player2.id = :pid) AND m.status = 'SCHEDULED' ORDER BY m.roundNumber ASC",
                            Match.class)
                    .setParameter("tid", tournamentId)
                    .setParameter("pid", playerId)
                    .getResultStream()
                    .findFirst();
        }
    }

    public List<Match> findByTournamentOrdered(Long tournamentId) {
        try (var em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT m FROM Match m WHERE m.tournament.id = :tid ORDER BY m.roundNumber ASC, m.bracketPosition ASC",
                            Match.class)
                    .setParameter("tid", tournamentId)
                    .getResultList();
        }
    }

    public boolean existsUnfinishedMatch(Long tournamentId, int round) {
        try (var em = emf.createEntityManager()) {
            Long count = em.createQuery(
                            "SELECT COUNT(m) FROM Match m WHERE m.tournament.id = :tid AND m.roundNumber = :round AND m.status <> 'FINISHED'",
                            Long.class)
                    .setParameter("tid", tournamentId)
                    .setParameter("round", round)
                    .getSingleResult();
            return count > 0;
        }
    }
}
