package be.technifutur.tournament.daos;

import be.technifutur.tournament.entities.Registration;

import java.util.List;
import java.util.Optional;

public class RegistrationDAO extends CrudDao<Registration, Integer> {

    public List<Registration> findByTournamentId(Long tournamentId) {
        try (var em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT r FROM Registration r WHERE r.tournament.id = :tid", Registration.class)
                    .setParameter("tid", tournamentId)
                    .getResultList();
        }
    }

    public List<Registration> findByPlayerId(Long playerId) {
        try (var em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT r FROM Registration r WHERE r.player.id = :pid", Registration.class)
                    .setParameter("pid", playerId)
                    .getResultList();
        }
    }

    public List<Registration> findByStatus(String status) {
        try (var em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT r FROM Registration r WHERE r.status = :status", Registration.class)
                    .setParameter("status", status)
                    .getResultList();
        }
    }

    public Optional<Registration> findByTournamentIdAndPlayerId(Long tournamentId, Long playerId) {
        try (var em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT r FROM Registration r WHERE r.tournament.id = :tid AND r.player.id = :pid", Registration.class)
                    .setParameter("tid", tournamentId)
                    .setParameter("pid", playerId)
                    .getResultStream()
                    .findFirst();
        }
    }
}

