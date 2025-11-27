package app.daos;

import app.dtos.WheelSegmentDTO;
import app.entities.WheelSegment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class WheelSegmentDAO
{
    private final EntityManagerFactory emf;

    public WheelSegmentDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    // GET ALL
    public List<WheelSegment> getAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.createQuery("SELECT w FROM WheelSegment w", WheelSegment.class).getResultList();
        }
    }

    // GET BY ID
    public WheelSegment getById(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.find(WheelSegment.class, id);
        }
    }

    // CREATE
    public void create(WheelSegment segment)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.persist(segment);
            em.getTransaction().commit();
        }
    }

    // UPDATE
    public WheelSegmentDTO update(int id, WheelSegmentDTO dto) throws Exception
    {
        EntityManager em = emf.createEntityManager();
        WheelSegmentDTO updatedDTO;

        try
        {
            em.getTransaction().begin();

            WheelSegment existing = em.find(WheelSegment.class, id);

            if (existing == null)
            {
                throw new Exception("Wheel segment with id " + id + " not found");
            }

            // Opdater felter baseret på DTO
            existing.setPosition(dto.getPosition());
            existing.setType(dto.getType());
            existing.setTitle(dto.getTitle());
            existing.setImageUrl(dto.getImageUrl());
            existing.setPrizeName(dto.getPrizeName());
            existing.setDiscountCode(dto.getDiscountCode());
            existing.setProductSku(dto.getProductSku());
            existing.setActive(dto.isActive());

            WheelSegment merged = em.merge(existing);
            updatedDTO = new WheelSegmentDTO(merged.getId(), merged.getWheel() != null ? merged.getWheel().getId() : null, merged.getPosition(), merged.getType(), merged.getTitle(), merged.getImageUrl(), merged.getPrizeName(), merged.getDiscountCode(), merged.getProductSku(), merged.isActive());

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

        return updatedDTO;
    }

    // DELETE
    public void delete(Long id)
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

            em.remove(segment);

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
