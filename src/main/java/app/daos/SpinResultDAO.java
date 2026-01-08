package app.daos;

import app.entities.SpinResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class SpinResultDAO
{

    private final EntityManagerFactory emf;

    public SpinResultDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }


    public List<SpinResult> getAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.createQuery("SELECT s FROM SpinResult s", SpinResult.class).getResultList();
        }
    }


    public SpinResult getById(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.find(SpinResult.class, id);
        }
    }


    public SpinResult create(SpinResult spinResult)
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            em.persist(spinResult);
            em.getTransaction().commit();
            return spinResult;

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
    }


    public SpinResult update(Long id, SpinResult updatedSpinResult) throws Exception
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();

            SpinResult existing = em.find(SpinResult.class, id);

            if (existing == null)
            {
                throw new Exception("SpinResult with id " + id + " not found");
            }

            // opdater kun relevante felter
            existing.setCustomerId(updatedSpinResult.getCustomerId());
            existing.setSubscriptionId(updatedSpinResult.getSubscriptionId());
            existing.setDeliveryId(updatedSpinResult.getDeliveryId());
            existing.setWheelSegment(updatedSpinResult.getWheelSegment());
            existing.setResultType(updatedSpinResult.getResultType());
            existing.setPrizeApplied(updatedSpinResult.isPrizeApplied());
            existing.setCreatedAt(updatedSpinResult.getCreatedAt());

            SpinResult merged = em.merge(existing);

            em.getTransaction().commit();
            return merged;

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
    }


    public void delete(Long id)
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();

            SpinResult spinResult = em.find(SpinResult.class, id);

            if (spinResult == null)
            {
                throw new IllegalArgumentException("SpinResult not found: " + id);
            }

            em.remove(spinResult);
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
    }


    public SpinResult findByCustomerAndDelivery(Long customerId, Long deliveryId)
    {
        try (EntityManager em = emf.createEntityManager())
        {

            List<SpinResult> results = em.createQuery("SELECT s FROM SpinResult s " + "WHERE s.customerId = :customerId " + "AND s.deliveryId = :deliveryId " + "ORDER BY s.createdAt DESC", SpinResult.class).setParameter("customerId", customerId).setParameter("deliveryId", deliveryId).setMaxResults(1).getResultList();

            return results.isEmpty() ? null : results.get(0);
        }
    }
}
