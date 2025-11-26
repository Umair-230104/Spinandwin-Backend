package app.daos;

import app.entities.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class CustomerDAO
{
    private final EntityManagerFactory emf;

    public CustomerDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }


    // GET ALL
    public List<Customer> getAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.createQuery("SELECT i FROM Customer i", Customer.class).getResultList();
        }
    }

    // GET BY ID
    public Customer getById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Customer.class, id);
        }
    }

    // CREATE
    public void create(Customer customer)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.persist(customer);
            em.getTransaction().commit();
        }
    }

    // DELETE
    public void delete(Long id) throws Exception
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Customer customer = em.find(Customer.class, id);
            if (customer != null)
            {
                em.remove(customer);
            } else
            {
                throw new Exception("Customer not found");
            }
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


    // UPDATE
    // tilføj update metode her

}
