package be.technifutur.tournament.daos;

import be.technifutur.tournament.entities.Tournament;

import java.util.List;
import java.util.Optional;

public class TournamentDAO extends CrudDao<Tournament, Integer> {

    public List<Tournament> findByStatus(String status) {
        try (var em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT t FROM Tournament t WHERE t.status = :status", Tournament.class)
                    .setParameter("status", status)
                    .getResultList();
        }
    }

    public Optional<Tournament> findByName(String name) {
        try (var em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT t FROM Tournament t WHERE t.name = :name", Tournament.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst();
        }
    }

    public List<Tournament> findUpcoming() {
        try (var em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT t FROM Tournament t WHERE t.status <> 'COMPLETED'", Tournament.class)
                    .getResultList();
        }
    }
}
