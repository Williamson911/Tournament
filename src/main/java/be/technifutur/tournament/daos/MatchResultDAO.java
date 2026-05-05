package be.technifutur.tournament.daos;
import be.technifutur.tournament.entities.MatchResult;

import java.util.List;
import java.util.Optional;

public class MatchResultDAO extends CrudDao<MatchResult, Integer> {


    public int countByWinner(int winnerId) {
        try (var em = emf.createEntityManager()) {
            Long count = em.createQuery(
                            "SELECT COUNT(m) FROM MatchResult m WHERE m.winner.id = :wid",
                            Long.class)
                    .setParameter("wid", winnerId)
                    .getSingleResult();

            return count.intValue();
        }
    }

    public List<MatchResult> findByTournament(int tournamentId) {
        try (var em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT m FROM MatchResult m WHERE m.match.tournament.id = :tid",
                            MatchResult.class)
                    .setParameter("tid", tournamentId)
                    .getResultList();
        }
    }

    public List<Object[]> countWinsByPlayer(int tournamentId) {
        try (var em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT m.winner.id, COUNT(m) " +
                                    "FROM MatchResult m " +
                                    "WHERE m.match.tournament.id = :tid " +
                                    "GROUP BY m.winner.id " +
                                    "ORDER BY COUNT(m) DESC",
                            Object[].class)
                    .setParameter("tid", tournamentId)
                    .getResultList();
        }
    }
}



