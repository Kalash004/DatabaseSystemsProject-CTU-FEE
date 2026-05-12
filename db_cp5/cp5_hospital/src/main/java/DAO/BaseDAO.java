package DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public abstract class BaseDAO<T, ID> {

    protected EntityManager entityManager;
    private final Class<T> entityClass;

    public BaseDAO(EntityManager entityManager, Class<T> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
    }

    // CREATE
    public void insert(T entity) {
        entityManager.getTransaction().begin();
        entityManager.persist(entity);
        entityManager.getTransaction().commit();
    }

    // READ (Single)
    public T selectById(ID id) {
        return entityManager.find(entityClass, id);
    }

    // READ (All)
    public List<T> selectAll() {
        TypedQuery<T> query = entityManager.createQuery("FROM " + entityClass.getSimpleName(), entityClass);
        return query.getResultList();
    }

    // UPDATE
    public T update(T entity) {
        entityManager.getTransaction().begin();
        T mergedEntity = entityManager.merge(entity);
        entityManager.getTransaction().commit();
        return mergedEntity;
    }

    // DELETE (by entity)
    public void delete(T entity) {
        entityManager.getTransaction().begin();
        if (!entityManager.contains(entity)) {
            entity = entityManager.merge(entity);
        }
        entityManager.remove(entity);
        entityManager.getTransaction().commit();
    }

    // DELETE (by ID)
    public void deleteById(ID id) {
        T entity = selectById(id);
        if (entity != null) {
            delete(entity);
        }
    }
}