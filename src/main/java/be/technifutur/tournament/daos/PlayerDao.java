package be.technifutur.tournament.daos;

import be.technifutur.tournament.utils.EMFProvider;
import be.technifutur.tournament.entities.Player;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;


@ApplicationScoped
public class PlayerDao extends CrudDao<Player, Integer> {
    @Inject
    public PlayerDao(EMFProvider emfProvider) {
        super(emfProvider);
    }

    @Override
    public List<Player> findAll() {
        try(var em = emfProvider.get().createEntityManager()) {
            return em.createQuery("SELECT p FROM Player p JOIN FETCH p.fighterMain", Player.class).getResultList();
        }
    }

    public Optional<Player> findByIdWithFighter(Integer idPlayer){
        try(EntityManager em = emfProvider.get().createEntityManager()){
            return em.createQuery(
                            "SELECT p FROM Player p JOIN FETCH p.fighterMain u WHERE p.id= :idPlayer",
                            Player.class)
                    .setParameter("idPlayer",idPlayer)
                    .getResultStream()
                    .findFirst();
        }
    }

    public Optional<Player> findByUsernameWithFighter(String username) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            Player player = em.createQuery("SELECT p FROM Player p JOIN FETCH p.fighterMain WHERE p.username = :username ", Player.class)
                    .setParameter("username", username)
                    .getSingleResult();

            return Optional.ofNullable(player);
        }
    }

    public Optional<Player> findByEmailWithFighter(String email) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            Player player = em.createQuery("SELECT p FROM Player p JOIN FETCH p.fighterMain WHERE p.email = :email ", Player.class)
                    .setParameter(email, email)
                    .getSingleResult();

            return Optional.ofNullable(player);
        }
    }

}
