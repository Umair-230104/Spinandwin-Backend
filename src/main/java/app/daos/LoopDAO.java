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

public class LoopDAO {

    private final EntityManagerFactory emf;

    public LoopDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Gemmer både:
     *  - kunder med aktivt abonnement (customersById)
     *  - aktive subscriptions (loopSubscriptionDTOList)
     *
     * Forventning:
     *  - customersById er keyed på Loop customer id (c.getId())
     *  - hver LoopSubscriptionDTO har en customer-ref med id (s.getCustomer().getId())
     */
    public void saveLoopDataToDb(List<LoopSubscriptionDTO> loopSubscriptionDTOList,
                                 Map<Long, LoopCustomerDTO> customersById) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // 1) Gem kunderne først
            Map<Long, Customer> customerEntitiesByLoopId = new HashMap<>();

            for (LoopCustomerDTO customerDTO : customersById.values()) {
                // Brug din ctor Customer(LoopCustomerDTO dto)
                Customer customer = new Customer(customerDTO);
                em.persist(customer);

                // key = loop customer id fra DTO
                customerEntitiesByLoopId.put(customerDTO.getLoopCustomerId(), customer);
            }

            // 2) Gem subscriptions og forbind dem til den rigtige Customer
            for (LoopSubscriptionDTO subDTO : loopSubscriptionDTOList) {

                // Opret Subscription ud fra DTO
                Subscription subscription = new Subscription(subDTO);

                // Find evt. tilhørende loop-customer-id på DTO'en
                if (subDTO.getCustomer() != null && subDTO.getCustomer().getId() != null) {
                    Long loopCustomerId = subDTO.getCustomer().getId();

                    Customer customer = customerEntitiesByLoopId.get(loopCustomerId);
                    if (customer != null) {
                        // Sørg for at Subscription har en setter til customer
                        subscription.setCustomer(customer);
                    }
                }

                em.persist(subscription);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            // Fejl -> rollback og kast videre / log
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace(); // kun fejl-print, som du ønskede
        } finally {
            em.close();
        }
    }
}
