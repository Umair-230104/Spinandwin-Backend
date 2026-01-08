package app.daos;

import app.dtos.LoopCustomerDTO;
import app.dtos.LoopSubscriptionDTO;
import app.entities.Customer;
import app.entities.Subscription;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoopDAO
{

    private final EntityManagerFactory emf;

    public LoopDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    public void saveLoopDataToDb(List<LoopSubscriptionDTO> loopSubscriptionDTOList, Map<Long, LoopCustomerDTO> customersById)
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();

            Map<Long, Customer> customerEntitiesByLoopId = new HashMap<>();

            for (LoopCustomerDTO customerDTO : customersById.values())
            {
                Customer customer = new Customer(customerDTO);
                em.persist(customer);

                customerEntitiesByLoopId.put(customerDTO.getLoopCustomerId(), customer);
            }

            for (LoopSubscriptionDTO subDTO : loopSubscriptionDTOList)
            {

                Subscription subscription = new Subscription(subDTO);

                if (subDTO.getCustomer() != null && subDTO.getCustomer().getId() != null)
                {
                    Long loopCustomerId = subDTO.getCustomer().getId();

                    Customer customer = customerEntitiesByLoopId.get(loopCustomerId);
                    if (customer != null)
                    {
                        subscription.setCustomer(customer);
                    }
                }

                em.persist(subscription);
            }

            em.getTransaction().commit();
        } catch (Exception e)
        {
            if (em.getTransaction().isActive())
            {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally
        {
            em.close();
        }
    }
}
