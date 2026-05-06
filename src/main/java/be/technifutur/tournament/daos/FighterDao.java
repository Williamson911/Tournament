package be.technifutur.tournament.daos;

import be.technifutur.tournament.entities.Fighter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class FighterDao extends CrudDao<Fighter,Integer>{
    public Fighter getFighterByName(String name){
        try(EntityManager em = emf.createEntityManager()){
            Fighter fighter =  em.createQuery("SELECT f FROM Fighter f WHERE f.name ILIKE :name",Fighter.class)
                    .setParameter("name",name)
                    .getSingleResult();
            return fighter;
        }
    }
}
