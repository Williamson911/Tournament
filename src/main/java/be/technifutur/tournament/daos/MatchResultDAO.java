package be.technifutur.tournament.daos;

import be.technifutur.tournament.entities.Match;
import be.technifutur.tournament.entities.MatchResult;

import java.util.List;
import java.util.Optional;

public class MatchResultDAO extends CrudDao<Match, Integer> {

    //findByWinner(Long playerId) — stat : combien de matchs gagnés
    //findByTournament(Long tournamentId) — tous les résultats d'un tournoi (classement final)
    //countWinsByPlayer(Long tournamentId) — leaderboard du tournoi

    public List<MatchResult> findByWinner(int tournamentId, int round) {
        try (var em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT m FROM Match m WHERE m.tournament.id = :tid AND m.roundNumber = :round",
                            Match.class)
                    .setParameter("tid", tournamentId)
                    .setParameter("round", round)
                    .getResultList();
        }
    }

    public Optional<MatchResult> findNextMatchForPlayer(Long playerId, Long tournamentId) {
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

    public List<MatchResult> findByTournament(int tournamentId) {
        try (var em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT m FROM Match m WHERE m.tournament.id = :tid ORDER BY m.roundNumber ASC, m.bracketPosition ASC",
                            Match.class)
                    .setParameter("tid", tournamentId)
                    .getResultList();
        }
    }

    public boolean countWinsByPlayer(int tournamentId, int round) {
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



