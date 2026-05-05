package be.technifutur.tournament.daos;

import be.technifutur.tournament.entities.Fighter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class FighterDao extends CrudDao<Fighter,Integer>{

//    public Optional<Character> findByEmailOrUsername(String login){
//        try(EntityManager em = emf.createEntityManager()){
//            Character user = em.createQuery("SELECT u FROM User u JOIN FETCH u.roles WHERE u.email = :login OR u.username = :login",User.class)
//                    .setParameter("login",login)
//                    .getSingleResult();
//
//            return Optional.ofNullable(user);
//        }
//    }

//    public boolean existsByEmail(@NotBlank String email) {
//
//        try(EntityManager em = emf.createEntityManager()){
//            return em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email",Long.class)
//                    .setParameter("email",email)
//                    .getSingleResult() > 0;
//        }
//    }

//    public List<Fighter> getAllFighters(){
//        try(EntityManager em = emf.createEntityManager()){
//            return em.createQuery("SELECT f FROM Fighter f",Fighter.class).getResultList();
//        }
//    }



    public Fighter getFighterByName(String name){
        try(EntityManager em = emf.createEntityManager()){
            Fighter fighter =  em.createQuery("SELECT f FROM Fighter f WHERE f.name ILIKE :name",Fighter.class)
                    .setParameter("name",name)
                    .getSingleResult();
            return fighter;
        }
    }

}
