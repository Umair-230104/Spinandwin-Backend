package app.unittest;

import app.Enums.DeliveryStatus;
import app.daos.DeliveryDAO;
import app.entities.Customer;
import app.entities.Delivery;
import app.entities.Subscription;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryDAOTest extends DaoTestBase
{

    private DeliveryDAO deliveryDAO;
    private Long customerId; // 👈 TILFØJ HER

    @Override
    @BeforeEach
    void beforeEach() {
        deliveryDAO = new DeliveryDAO(emf);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Customer c = new Customer();
        em.persist(c);
        em.flush();
        customerId = c.getId();

        Subscription s = new Subscription();
        s.setCustomer(c);
        s.setLoopSubscriptionId(111L);
        em.persist(s);

        Delivery oldDelivery = new Delivery();
        oldDelivery.setSubscription(s);
        oldDelivery.setStatus(DeliveryStatus.SHIPPED);
        oldDelivery.setShippedAt(LocalDateTime.now().minusDays(2));
        em.persist(oldDelivery);

        Delivery latestDelivery = new Delivery();
        latestDelivery.setSubscription(s);
        latestDelivery.setStatus(DeliveryStatus.SHIPPED);
        latestDelivery.setShippedAt(LocalDateTime.now());
        em.persist(latestDelivery);

        em.getTransaction().commit();
        em.close();
    }


    @Override
    @AfterEach
    void afterEach()
    {
        cleanDb("DELETE FROM Delivery", "DELETE FROM Subscription", "DELETE FROM Customer");
    }

    @Test
    void existsDeliveredByCustomerId_returnsTrue()
    {
        assertTrue(deliveryDAO.existsDeliveredByCustomerId(customerId));
    }

    @Test
    void existsDeliveredByCustomerId_returnsFalse_whenNoShippedDeliveries()
    {
        assertFalse(deliveryDAO.existsDeliveredByCustomerId(999L));
    }

    @Test
    void findLatestDeliveredByCustomer_returnsLatestDelivery() {
        Delivery result = deliveryDAO.findLatestDeliveredByCustomer(customerId);
        assertNotNull(result);
        assertEquals(DeliveryStatus.SHIPPED, result.getStatus());
    }

    @Test
    void findLatestDeliveredByCustomer_returnsNull_whenNoDeliveries() {
        Delivery result = deliveryDAO.findLatestDeliveredByCustomer(999L);
        assertNull(result);
    }
}
