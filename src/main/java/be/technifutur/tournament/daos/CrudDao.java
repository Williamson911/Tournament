package be.technifutur.tournament.daos;

import be.technifutur.tournament.utils.EmfFactory;
import jakarta.persistence.EntityManagerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public abstract class CrudDao<T, ID> {



    protected final EntityManagerFactory emf;
    private final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    public CrudDao() {
        this.emf = EmfFactory.getEmf();
        Type superclass = getClass().getGenericSuperclass();
        while (!(superclass instanceof ParameterizedType)) {
            superclass = ((Class<?>) superclass).getGenericSuperclass();
        }
        this.entityClass = (Class<T>)
                ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    public List<T> findAll() {
        try(var em = emf.createEntityManager()) {
            return em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                    .getResultList();
        }
    }

    public Optional<T> findById(ID id) {
        try(var em = emf.createEntityManager()) {
            return Optional.ofNullable(em.find(entityClass, id));
        }
    }

    public T save(T entity) {
        try(var em = emf.createEntityManager()) {
            var tx = em.getTransaction();
            tx.begin();
            em.persist(entity);
            tx.commit();
            return entity;
        }
    }

    public void saveAll(List<T> list) {
        try(var em = emf.createEntityManager()) {
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

    public T update(T entity) {
        try(var em = emf.createEntityManager()) {
            var tx = em.getTransaction();
            tx.begin();
            T merged = em.merge(entity);
            tx.commit();
            return merged;
        }
    }

    public T delete(ID id) {
        try(var em = emf.createEntityManager()) {
            var tx = em.getTransaction();
            tx.begin();
            T ref = em.getReference(entityClass, id);
            em.remove(ref);
            tx.commit();
            return ref;
        }
    }

    public boolean existsById(ID id) {
        try(var em = emf.createEntityManager()) {
            return em.createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e WHERE e.id = :id", Long.class)
                    .setParameter("id", id)
                    .getSingleResult() > 0;
        }
    }

    public long count() {
        try(var em = emf.createEntityManager()) {
            return em.createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
                    .getSingleResult();
        }
    }
}