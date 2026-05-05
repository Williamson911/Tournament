package be.technifutur.tournament.daos;

import be.technifutur.tournament.entities.Player;
import jakarta.persistence.EntityManager;

import java.util.Optional;

public class PlayerDao extends CrudDao<Player, Integer> {

    public Optional<Player> findByEmail(String login) {
        try (EntityManager em = emf.createEntityManager()) {
            Player player = em.createQuery("SELECT p FROM Player p JOIN FETCH p.email " +
                            "WHERE p.email = :login ", Player.class)
                    .setParameter("login", login)
                    .getSingleResult();

            return Optional.ofNullable(player);
        }
    }

    public boolean existsByEmail(String email) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT COUNT(p) FROM Player p WHERE p.email = :email", Long.class)
                    .setParameter("email", email)
                    .getSingleResult() > 0;
        }
    }

    public boolean existsByUsername(String username) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT COUNT(p) FROM Player p WHERE p.username = :username", Long.class)
                    .setParameter("username", username)
                    .getSingleResult() > 0;
        }
    }
}
