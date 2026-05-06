package be.technifutur.tournament.daos;

import be.technifutur.tournament.entities.Fighter;
import be.technifutur.tournament.entities.Player;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PlayerDao extends CrudDao<Player, Integer> {


    @Override
    public List<Player> findAll() {
        try(var em = emf.createEntityManager()) {
            return em.createQuery("SELECT p FROM Player p JOIN FETCH p.fighterMain", Player.class).getResultList();
        }
    }

    public Optional<Player> findByIdWithFighter(Integer idPlayer){
        try(EntityManager em = emf.createEntityManager()){
            return em.createQuery(
                            "SELECT p FROM Player p JOIN FETCH p.fighterMain u WHERE p.id= :idPlayer",
                            Player.class)
                    .setParameter("idPlayer",idPlayer)
                    .getResultStream()
                    .findFirst();
        }
    }

    public Optional<Player> findByUsernameWithFighter(String username) {
        try (EntityManager em = emf.createEntityManager()) {
            Player player = em.createQuery("SELECT p FROM Player p JOIN FETCH p.fighterMain WHERE p.username = :username ", Player.class)
                    .setParameter("username", username)
                    .getSingleResult();

            return Optional.ofNullable(player);
        }
    }

    public Optional<Player> findByEmailWithFighter(String email) {
        try (EntityManager em = emf.createEntityManager()) {
            Player player = em.createQuery("SELECT p FROM Player p JOIN FETCH p.fighterMain WHERE p.email = :email ", Player.class)
                    .setParameter(email, email)
                    .getSingleResult();

            return Optional.ofNullable(player);
        }
    }

}
