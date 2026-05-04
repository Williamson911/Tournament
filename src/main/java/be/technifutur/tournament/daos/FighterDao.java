package be.technifutur.tournament.daos;

import be.technifutur.tournament.entities.Fighter;
import jakarta.persistence.EntityManager;

public class FighterDao extends CrudDao<Fighter, Long> {

    public Fighter getFighterByName(String name) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT f FROM Fighter f WHERE f.name = :name", Fighter.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }

}
