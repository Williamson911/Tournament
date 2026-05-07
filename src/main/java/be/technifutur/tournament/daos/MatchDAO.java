package be.technifutur.tournament.daos;

import be.technifutur.tournament.dtos.MatchDTO;
import be.technifutur.tournament.entities.Player;
import be.technifutur.tournament.utils.EMFProvider;
import be.technifutur.tournament.entities.Match;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MatchDAO extends CrudDao<Match, Integer> {

    @Inject
    public MatchDAO(EMFProvider emfProvider) {
        super(emfProvider);
    }

    @Override
    public Optional<Match> findById(Integer id) {
        try(var em = emfProvider.get().createEntityManager()) {
            return em.createQuery("SELECT m FROM Match m JOIN FETCH m.tournament " +
                    "            JOIN FETCH m.player1 " +
                    "            JOIN FETCH m.player2 " +
                    "   WHERE m.id = :id", Match.class).setParameter("id", id).getResultStream().findFirst();
        }
    }

    public List<Match> findByTournamentAndRound(int tournamentId, int round) {
        try (var em = emfProvider.get().createEntityManager()) {
            return em.createQuery(
                            "SELECT m FROM Match m WHERE m.tournament.id = :tid AND m.numberRounds = :round",
                            Match.class)
                    .setParameter("tid", tournamentId)
                    .setParameter("round", round)
                    .getResultList();
        }
    }

    public Optional<Match> findNextMatchForPlayer(int playerId, int tournamentId) {
        try (var em = emfProvider.get().createEntityManager()) {
            return em.createQuery(
                            "SELECT m FROM Match m WHERE m.tournament.id = :tid AND (m.player1.id = :pid OR m.player2.id = :pid) AND m.status = 'SCHEDULED' ORDER BY m.numberRounds ASC",
                            Match.class)
                    .setParameter("tid", tournamentId)
                    .setParameter("pid", playerId)
                    .getResultStream()
                    .findFirst();
        }
    }

    public List<Match> findByTournamentOrdered(int tournamentId) {
        try (var em = emfProvider.get().createEntityManager()) {
            return em.createQuery(
                            "SELECT m FROM Match m WHERE m.tournament.id = :tid ORDER BY m.numberRounds ASC, m.bracketPosition ASC",
                            Match.class)
                    .setParameter("tid", tournamentId)
                    .getResultList();
        }
    }

    public boolean existsUnfinishedMatch(int tournamentId, int round) {
        try (var em = emfProvider.get().createEntityManager()) {
            Long count = em.createQuery(
                            "SELECT COUNT(m) FROM Match m WHERE m.tournament.id = :tid AND m.numberRounds = :round AND m.status <> 'FINISHED'",
                            Long.class)
                    .setParameter("tid", tournamentId)
                    .setParameter("round", round)
                    .getSingleResult();
            return count > 0;
        }
    }
    public List<Match> findAllWithRelations() {
        try (var em = emfProvider.get().createEntityManager()) {
            return em.createQuery("""
            SELECT m FROM Match m
            JOIN FETCH m.tournament
            JOIN FETCH m.player1
            JOIN FETCH m.player2
        """, Match.class).getResultList();
        }
    }

}
