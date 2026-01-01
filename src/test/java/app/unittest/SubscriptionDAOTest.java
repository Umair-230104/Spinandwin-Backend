package app.unittest;

import app.Enums.SubscriptionStatus;
import app.daos.SubscriptionDAO;
import app.entities.Customer;
import app.entities.Subscription;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionDAOTest extends DaoTestBase
{

    private SubscriptionDAO subscriptionDAO;
    private Long customerId; // 👈 vigtig

    @Override
    @BeforeEach
    void beforeEach()
    {
        subscriptionDAO = new SubscriptionDAO(emf);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Customer c = new Customer();
        em.persist(c);
        em.flush();                 // 👈 tving ID
        customerId = c.getId();     // 👈 gem ID

        Subscription s = new Subscription();
        s.setCustomer(c);
        s.setStatus(SubscriptionStatus.ACTIVE);
        s.setLoopSubscriptionId(12345L); // 👈 VIGTIG
        em.persist(s);

        em.getTransaction().commit();
        em.close();
    }

    @Override
    @AfterEach
    void afterEach()
    {
        cleanDb("DELETE FROM Subscription", "DELETE FROM Customer");
    }

    @Test
    void existsByCustomerIdAndStatus_returnsTrue()
    {
        assertTrue(subscriptionDAO.existsByCustomerIdAndStatus(customerId, SubscriptionStatus.ACTIVE));
    }

    @Test
    void existsByCustomerIdAndStatus_returnsFalse()
    {
        assertFalse(subscriptionDAO.existsByCustomerIdAndStatus(1L, SubscriptionStatus.CANCELLED));
    }

    @Test
    void findByLoopSubscriptionId_returnsSubscription_whenExists() {
        Subscription result = subscriptionDAO.findByLoopSubscriptionId(12345L);
        assertNotNull(result);
        assertEquals(12345L, result.getLoopSubscriptionId());
    }

    @Test
    void findByLoopSubscriptionId_returnsNull_whenNotExists() {
        Subscription result = subscriptionDAO.findByLoopSubscriptionId(999L);
        assertNull(result);
    }

}
