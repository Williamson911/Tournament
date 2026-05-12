package be.technifutur.tournament.dal;

import be.technifutur.tournament.dl.entity.Player;
import be.technifutur.tournament.dl.entity.Tournament;
import be.technifutur.tournament.utils.EMFProvider;
import be.technifutur.tournament.dl.entity.Registration;
import be.technifutur.tournament.dl.enums.RegistrationStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class RegistrationDAO extends CrudDao<Registration, Integer> {

    @Inject
    public RegistrationDAO(EMFProvider emfProvider) {
        super(emfProvider);
    }

    public boolean existsByPlayerIdAndTournamentId(int playerId, int tournamentId) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            return em.createQuery(
                            "SELECT count(r) FROM Registration r WHERE r.player.id = :pid " +
                                    "AND r.tournament.id = :tid", Long.class)
                    .setParameter("pid", playerId)
                    .setParameter("tid", tournamentId)
                    .getSingleResult() > 0;
        }
    }

    public boolean existsByTournamentId(int tournamentId) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            return em.createQuery(
                            "SELECT count(r) FROM Registration r WHERE r.tournament.id = :tid", Long.class)
                    .setParameter("tid", tournamentId)
                    .getSingleResult() > 0;
        }
    }

    public void registerPlayerToTournament(int playerId, int tournamentId) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            em.getTransaction().begin();
            Registration r = Registration.builder()
                    .player(em.getReference(Player.class, playerId))
                    .tournament(em.getReference(Tournament.class, tournamentId))
                    .registrationStatus(RegistrationStatus.PENDING)
                    .build();
            em.persist(r);
            em.getTransaction().commit();
        }
    }

    public void unregisterPlayerFromTournament(int playerId, int tournamentId) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery(
                            "DELETE FROM Registration r WHERE r.player.id = :pid " +
                                    "AND r.tournament.id = :tid")
                    .setParameter("pid", playerId)
                    .setParameter("tid", tournamentId)
                    .executeUpdate();
            em.getTransaction()
                    .commit();
        }
    }

    public void updateStatusRegistration(int tournamentId, RegistrationStatus newStatus) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery(
                            "UPDATE Registration r SET r.registrationStatus = :newStatus " +
                                    "WHERE r.tournament.id = :tid")
                    .setParameter("newStatus", newStatus)
                    .setParameter("tid", tournamentId)
                    .executeUpdate();
            em.getTransaction().commit();
        }
    }

    public int countByTournamentId(int tournamentId) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            return (int) em.createQuery(
                            "SELECT COUNT(r) FROM Registration r WHERE r.tournament.id = :tid")
                    .setParameter("tid", tournamentId)
                    .getSingleResult();
        }
    }

    public List<Player> getRegisteredPlayersForTournament(int tournamentId) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            return em.createQuery(
                            "SELECT r.player FROM Registration r WHERE r.tournament.id = :tid", Player.class)
                    .setParameter("tid", tournamentId)
                    .getResultList();
        }
    }

    public List<Tournament> getRegisteredTournamentForPlayers(int playerId) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            return em.createQuery(
                            "SELECT r.tournament FROM Registration r WHERE r.player.id = :pid", Tournament.class)
                    .setParameter("pid", playerId)
                    .getResultList();
        }
    }

    public Optional<Registration> findByPlayerAndTournament(int playerId, int tournamentId) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            return em.createQuery(
                            "SELECT r FROM Registration r WHERE r.player.id = :pid " +
                                    "AND r.tournament.id = :tid", Registration.class)
                    .setParameter("pid", playerId)
                    .setParameter("tid", tournamentId)
                    .getResultStream()
                    .findFirst();
        }
    }

    public List<Registration> findByTournamentAndStatus(int tournamentId, RegistrationStatus status) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            return em.createQuery(
                            "SELECT r FROM Registration r WHERE r.tournament.id = :tid " +
                                    "AND r.registrationStatus = :status",
                            Registration.class)
                    .setParameter("tid", tournamentId)
                    .setParameter("status", status)
                    .getResultList();
        }
    }

    public List<Registration> findByTournamentWithStatus(int tournamentId) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            return em.createQuery(
                            "SELECT r FROM Registration r WHERE r.tournament.id = :tid " +
                                    "AND r.registrationStatus = :status",
                            Registration.class)
                    .setParameter("tid", tournamentId)
                    .setParameter("status", RegistrationStatus.CONFIRMED)
                    .getResultList();
        }
    }

    public void updateStatusForPlayers(int tournamentId, Set<Integer> playerIds, RegistrationStatus status) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery(
                            "UPDATE Registration r SET r.registrationStatus = :status " +
                                    "WHERE r.tournament.id = :tid AND r.player.id IN :playerIds")
                    .setParameter("status", status)
                    .setParameter("tid", tournamentId)
                    .setParameter("playerIds", playerIds)
                    .executeUpdate();
            em.getTransaction().commit();
        }
    }

    public void resetAllStatusForTournament(int tournamentId, RegistrationStatus status) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery(
                            "UPDATE Registration r SET r.registrationStatus = :status " +
                                    "WHERE r.tournament.id = :tid")
                    .setParameter("status", status)
                    .setParameter("tid", tournamentId)
                    .executeUpdate();
            em.getTransaction().commit();
        }
    }
}