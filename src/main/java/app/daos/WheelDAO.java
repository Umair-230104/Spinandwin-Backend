package app.daos;

import app.entities.WheelSegment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class WheelDAO
{

    private final EntityManagerFactory emf;

    public WheelDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    // CREATE
    public WheelSegment create(WheelSegment segment) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(segment);
            em.getTransaction().commit();
            return segment;
        }
    }

    // GET ALL
    public List<WheelSegment> getAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.createQuery("SELECT w FROM WheelSegment w", WheelSegment.class)
                    .getResultList();
        }
    }

    // UPDATE PRIZE FIELDS
    /** Opdaterer præmiefelter for et PRIZE-segment (prizeName, discountCode, productSku) samt title og imageUrl. */
    public WheelSegment updatePrizeFields(
            Long id,
            String title,
            String imageUrl,
            String prizeName,
            String discountCode
    )
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();

            WheelSegment segment = em.find(WheelSegment.class, id);

            if (segment == null)
            {
                throw new IllegalArgumentException("Wheel segment not found: " + id);
            }

            if (!segment.getType().name().equals("PRIZE"))
            {
                throw new IllegalStateException("This wheel segment is not a PRIZE segment.");
            }

            segment.setTitle(title);
            segment.setImageUrl(imageUrl);
            segment.setPrizeName(prizeName);
            segment.setDiscountCode(discountCode);

            WheelSegment updated = em.merge(segment);

            em.getTransaction().commit();
            return updated;

        } catch (Exception e)
        {
            em.getTransaction().rollback();
            throw e;

        } finally
        {
            em.close();
        }
    }

    // DELETE
    public void delete(Long id) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            WheelSegment segment = em.find(WheelSegment.class, id);

            if (segment == null) {
                throw new IllegalArgumentException("Wheel segment not found: " + id);
            }

            em.remove(segment);

            em.getTransaction().commit();

        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;

        } finally {
            em.close();
        }
    }
}
