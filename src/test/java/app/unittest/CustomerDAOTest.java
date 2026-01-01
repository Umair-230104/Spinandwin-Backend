package app.unittest;

import app.daos.CustomerDAO;
import app.entities.Customer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerDAOTest extends DaoTestBase
{

    private CustomerDAO customerDAO;

    @Override
    @BeforeEach
    void beforeEach()
    {
        customerDAO = new CustomerDAO(emf);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Customer c = new Customer();
        c.setEmail("test@mail.dk");
        c.setPhone("12345678");
        c.setShopifyId(777L);   // 👈 DET MANGLEDE
        em.persist(c);

        em.getTransaction().commit();
        em.close();
    }

    @Override
    @AfterEach
    void afterEach()
    {
        cleanDb("DELETE FROM Customer");
    }

    @Test
    void findByEmail_returnsCustomer_caseInsensitive()
    {
        Customer result = customerDAO.findSingleByEmailOrPhone("TEST@mail.dk");
        assertNotNull(result);
        assertEquals("test@mail.dk", result.getEmail());
    }

    @Test
    void findByPhone_returnsCustomer()
    {
        Customer result = customerDAO.findSingleByEmailOrPhone("12345678");
        assertNotNull(result);
        assertEquals("12345678", result.getPhone());
    }

    @Test
    void findUnknown_returnsNull()
    {
        Customer result = customerDAO.findSingleByEmailOrPhone("unknown");
        assertNull(result);
    }

    @Test
    void findByShopifyId_returnsCustomer_whenExists() {
        Customer result = customerDAO.findByShopifyId(777L);
        assertNotNull(result);
        assertEquals(777L, result.getShopifyId());
    }

    @Test
    void findByShopifyId_returnsNull_whenNotExists() {
        Customer result = customerDAO.findByShopifyId(999L);
        assertNull(result);
    }
}
