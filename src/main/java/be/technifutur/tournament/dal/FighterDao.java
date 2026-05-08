package be.technifutur.tournament.dal;

import be.technifutur.tournament.utils.EMFProvider;
import be.technifutur.tournament.dl.entities.Fighter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class FighterDao extends CrudDao<Fighter,Integer>{
    @Inject
    public FighterDao(EMFProvider emfProvider) {
        super(emfProvider);
    }


    public Fighter getFighterByName(String name){
        try(EntityManager em = emfProvider.get().createEntityManager()){
            Fighter fighter =  em.createQuery("SELECT f FROM Fighter f WHERE f.name ILIKE :name",Fighter.class)
                    .setParameter("name",name)
                    .getSingleResult();
            return fighter;
        }
    }
}
