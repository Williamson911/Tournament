package be.technifutur.tournament.dal;

import be.technifutur.tournament.utils.EMFProvider;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;


public abstract class CrudDao<TEntity, TId> {

    protected final EMFProvider emfProvider;

    private final Class<TEntity> entityClass;

    @SuppressWarnings("unchecked")
    public CrudDao(EMFProvider emfProvider) {
        this.emfProvider = emfProvider;
        Type superclass = getClass().getGenericSuperclass();
        while (!(superclass instanceof ParameterizedType)) {
            superclass = ((Class<?>) superclass).getGenericSuperclass();
        }
        this.entityClass = (Class<TEntity>)
                ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    public List<TEntity> findAll() {
        try(var em = emfProvider.get().createEntityManager()) {
            return em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                    .getResultList();
        }
    }

    public Optional<TEntity> findById(TId id) {
        try(var em = emfProvider.get().createEntityManager()) {
            return Optional.ofNullable(em.find(entityClass, id));
        }
    }

    public TEntity save(TEntity entity) {
        try(var em = emfProvider.get().createEntityManager()) {
            var tx = em.getTransaction();
            tx.begin();
//            em.persist(entity);
            TEntity managed = em.merge(entity);
            tx.commit();
//            return entity;
            return managed;
        }
    }

    public void saveAll(List<TEntity> list) {
        try(var em = emfProvider.get().createEntityManager()) {
            em.getTransaction().begin();

            int batchSize = 20;
            for(int i = 0; i < list.size(); i++) {
                em.persist(list.get(i));

                if ((i + 1) % batchSize == 0) {
                    em.flush();
                    em.clear();
                }
            }

            em.getTransaction().commit();
        }
    }

    public TEntity update(TEntity entity) {
        try(var em = emfProvider.get().createEntityManager()) {
            var tx = em.getTransaction();
            tx.begin();
            TEntity merged = em.merge(entity);
            tx.commit();
            return merged;
        }
    }

    public TEntity delete(TId id) {
        try(var em = emfProvider.get().createEntityManager()) {
            var tx = em.getTransaction();
            tx.begin();
            TEntity ref = em.getReference(entityClass,id);
            em.remove(ref);
            tx.commit();
            return ref;
        }
    }

    public boolean existsById(TId id) {
        try(var em = emfProvider.get().createEntityManager()) {
            return em.createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e WHERE e.id = :id", Long.class)
                    .setParameter("id", id)
                    .getSingleResult() > 0;
        }
    }

    public long count() {
        try(var em = emfProvider.get().createEntityManager()) {
            return em.createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
                    .getSingleResult();
        }
    }
}