package app.daos;

import app.entities.Subscription;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class SubscriptionDAO
{
    private final EntityManagerFactory emf;

    public SubscriptionDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    // GET ALL
    public List<Subscription> getAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.createQuery("SELECT s FROM Subscription s", Subscription.class).getResultList();
        }
    }

    // GET BY ID
    public Subscription getById(Long id)
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            return em.find(Subscription.class, id);
        } finally
        {
            em.close();
        }
    }

    // CREATE
    public void create(Subscription subscription)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.persist(subscription);
            em.getTransaction().commit();
        }
    }

    // UPDATE
    public Subscription update(Long id, Subscription updatedData) throws Exception
    {
        EntityManager em = emf.createEntityManager();
        Subscription updated;

        try
        {
            em.getTransaction().begin();

            Subscription existing = em.find(Subscription.class, id);

            if (existing == null)
            {
                throw new Exception("Subscription with id " + id + " not found");
            }

            // Set fields
            existing.setLoopSubscriptionId(updatedData.getLoopSubscriptionId());
            existing.setCustomer(updatedData.getCustomer());
            existing.setStatus(updatedData.getStatus());
            existing.setNextBillingAt(updatedData.getNextBillingAt());

            updated = em.merge(existing);

            em.getTransaction().commit();

        } catch (Exception e)
        {
            if (em.getTransaction().isActive())
            {
                em.getTransaction().rollback();
            }
            throw e;
        } finally
        {
            em.close();
        }

        return updated;
    }

    // DELETE
    public void delete(Long id)
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();

            Subscription subscription = em.find(Subscription.class, id);

            if (subscription == null)
            {
                throw new IllegalArgumentException("Subscription not found: " + id);
            }

            em.remove(subscription);

            em.getTransaction().commit();

        } catch (Exception e)
        {
            em.getTransaction().rollback();
            throw e;

        } finally
        {
            em.close();
        }
    }
}
