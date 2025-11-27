package app.daos;

import app.entities.Customer;
import app.dtos.LoopCustomerDTO;
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
    public LoopCustomerDTO update(Long id, LoopCustomerDTO dto) throws Exception {
        EntityManager em = emf.createEntityManager();
        LoopCustomerDTO updatedDTO;

        try {
            em.getTransaction().begin();

            Customer existingCustomer = em.find(Customer.class, id);

            if (existingCustomer != null) {

                // Update fields
                existingCustomer.setFirstName(dto.getFirstName());
                existingCustomer.setLastName(dto.getLastName());
                existingCustomer.setEmail(dto.getEmail());
                existingCustomer.setPhone(dto.getPhone());

                // loopCustomerId only if you want it updatable
                existingCustomer.setLoopCustomerId(dto.getLoopCustomerId());

                // Active subscription (boolean)
                existingCustomer.setActiveSubscription(
                        dto.getActiveSubscriptionsCount() != null &&
                                dto.getActiveSubscriptionsCount() > 0
                );

                Customer merged = em.merge(existingCustomer);
                updatedDTO = new LoopCustomerDTO(merged);

            } else {
                throw new Exception("Customer with id " + id + " not found");
            }

            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }

        return updatedDTO;
    }

}
