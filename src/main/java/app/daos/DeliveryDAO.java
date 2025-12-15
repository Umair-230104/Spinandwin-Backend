package app.daos;

import app.Enums.DeliveryStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class DeliveryDAO {

    private final EntityManagerFactory emf;

    public DeliveryDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    /**
     * Tjekker om kunden har mindst én levering,
     * hvor status = SHIPPED.
     *
     * Vi går via Delivery -> Subscription -> Customer.
     */
    public boolean existsDeliveredByCustomerId(Long customerId) {
        try (EntityManager em = emf.createEntityManager()) {

            String jpql = """
                    SELECT COUNT(d) FROM Delivery d
                    WHERE d.subscription.customer.id = :customerId
                      AND d.status = :status
                    """;

            Long count = em.createQuery(jpql, Long.class)
                    .setParameter("customerId", customerId)
                    .setParameter("status", DeliveryStatus.SHIPPED)
                    .getSingleResult();

            return count != null && count > 0;
        }
    }
}
