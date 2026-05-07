package be.technifutur.tournament.daos;

import be.technifutur.tournament.utils.EMFProvider;
import be.technifutur.tournament.entities.Registration;
import be.technifutur.tournament.enums.RegistrationStatus;
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

    //Permet de récupérer les inscriptions au tournoi pour pouvoir générer les matchs.
    public List<Registration> findByTournament(int tournamentId) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            return em.createQuery(
                            "SELECT r FROM Registration r WHERE r.tournament.id = :tid",
                            Registration.class)
                    .setParameter("tid", tournamentId)
                    .getResultList();
        }
    }

    /*
    Filtre de la méthode findByTournament :
    on prend  en compte seulement les joueurs ayant le statut confirmed
    */
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

    //Permet de récupérer la liste des tournois auxquels un joueur est inscrit
    public List<Registration> findByPlayer(int playerId) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            return em.createQuery(
                            "SELECT r FROM Registration r WHERE r.player.id = :pid",
                            Registration.class)
                    .setParameter("pid", playerId)
                    .getResultList();
        }
    }

    //Verifie si le joueur est déjà inscrit à un tournoi
    public boolean existsByPlayerAndTournament(int playerId, int tournamentId) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            return em.createQuery(
                            "SELECT count(r) FROM Registration r WHERE r.player.id = :pid " +
                                    "AND r.tournament.id = :tid", Long.class)
                    .setParameter("pid", playerId)
                    .setParameter("tid", tournamentId)
                    .getSingleResult() > 0;
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
}
