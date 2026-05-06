package be.technifutur.tournament.daos;

import be.technifutur.tournament.utils.EMFProvider;
import be.technifutur.tournament.entities.Tournament;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TournamentDAO extends CrudDao<Tournament, Integer> {

    @Inject
    public TournamentDAO(EMFProvider emfProvider) {
        super(emfProvider);
    }

    public List<Tournament> findByStatus(String status) {
        try (var em = emfProvider.get().createEntityManager()) {
            return em.createQuery(
                    "SELECT t FROM Tournament t WHERE t.status = :status", Tournament.class)
                    .setParameter("status", status)
                    .getResultList();
        }
    }

    public Optional<Tournament> findByName(String name) {
        try (var em = emfProvider.get().createEntityManager()) {
            return em.createQuery(
                    "SELECT t FROM Tournament t WHERE t.name = :name", Tournament.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst();
        }
    }

    public List<Tournament> findUpcoming() {
        try (var em = emfProvider.get().createEntityManager()) {
            return em.createQuery(
                    "SELECT t FROM Tournament t WHERE t.status <> 'COMPLETED'", Tournament.class)
                    .getResultList();
        }
    }
}
