package app.daos;

import app.dtos.LoopCustomerDTO;
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
            return em.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();
        }
    }

    // GET BY ID
    public Customer getById(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
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

    // UPDATE
    public LoopCustomerDTO update(Long id, LoopCustomerDTO dto) throws Exception
    {
        EntityManager em = emf.createEntityManager();
        LoopCustomerDTO updatedDTO;

        try
        {
            em.getTransaction().begin();

            Customer existingCustomer = em.find(Customer.class, id);

            if (existingCustomer != null)
            {

                // Update fields
                existingCustomer.setFirstName(dto.getFirstName());
                existingCustomer.setLastName(dto.getLastName());
                existingCustomer.setEmail(dto.getEmail());
                existingCustomer.setPhone(dto.getPhone());

                // loopCustomerId only if you want it updatable
                existingCustomer.setLoopCustomerId(dto.getLoopCustomerId());
                existingCustomer.setShopifyId(dto.getShopifyId());

                // Active subscription (boolean)
                existingCustomer.setActiveSubscription(
                        dto.getActiveSubscriptionsCount() != null &&
                                dto.getActiveSubscriptionsCount() > 0
                );

                Customer merged = em.merge(existingCustomer);
                updatedDTO = new LoopCustomerDTO(merged);

            } else
            {
                throw new Exception("Customer with id " + id + " not found");
            }

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


    public Customer findSingleByEmailOrPhone(String emailOrPhone)
    {
        try (EntityManager em = emf.createEntityManager())
        {

            String value = emailOrPhone.trim(); // fjern whitespace i begge ender

            String jpql = """
                    SELECT c FROM Customer c
                    WHERE (c.email IS NOT NULL AND lower(c.email) = lower(:value))
                       OR (c.phone IS NOT NULL AND c.phone = :value)
                    """;

            List<Customer> customers = em.createQuery(jpql, Customer.class)
                    .setParameter("value", value)
                    .getResultList();

            if (customers.isEmpty())
            {
                // ingen kunde fundet med hverken email eller telefon
                return null;
            }

            if (customers.size() > 1)
            {
                // Valgfrit: log eller smid exception, så du ved der er dataproblem
                // throw new IllegalStateException("Flere kunder fundet med: " + value);
            }

            // Tag første kunde (normalt vil der kun være én)
            return customers.get(0);
        }
    }

    public Customer findByShopifyId(Long shopifyId)
    {
        try (var em = emf.createEntityManager())
        {
            return em.createQuery(
                            "SELECT c FROM Customer c WHERE c.shopifyId = :sid", Customer.class)
                    .setParameter("sid", shopifyId)
                    .getSingleResult();
        } catch (Exception e)
        {
            return null;
        }
    }

    public void upsert(Customer customer)
    {
        try (var em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.merge(customer);
            em.getTransaction().commit();
        }
    }


}
