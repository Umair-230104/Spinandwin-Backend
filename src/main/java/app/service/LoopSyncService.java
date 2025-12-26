package app.service;

import app.Enums.SubscriptionStatus;
import app.daos.CustomerDAO;
import app.daos.SubscriptionDAO;
import app.dtos.LoopCustomerDTO;
import app.dtos.LoopSubscriptionDTO;
import app.entities.Customer;
import app.entities.Subscription;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class LoopSyncService {

    private final CustomerDAO customerDAO;
    private final SubscriptionDAO subscriptionDAO;

    public LoopSyncService(CustomerDAO customerDAO,
                           SubscriptionDAO subscriptionDAO) {
        this.customerDAO = customerDAO;
        this.subscriptionDAO = subscriptionDAO;
    }

    // ===================== CUSTOMER =====================

    /**
     * Opretter eller opdaterer customer baseret på shopifyId
     * Returnerer ALTID Customer entity (eller null hvis input er ugyldigt)
     */
    public Customer syncCustomer(LoopCustomerDTO dto) {

        if (dto == null || dto.getShopifyId() == null) return null;

        Customer existing = customerDAO.findByShopifyId(dto.getShopifyId());

        if (existing == null) {
            existing = new Customer(dto);
            customerDAO.create(existing);
        } else {
            existing.setFirstName(dto.getFirstName());
            existing.setLastName(dto.getLastName());
            existing.setEmail(dto.getEmail());
            existing.setPhone(dto.getPhone());
            existing.setLoopCustomerId(dto.getLoopCustomerId());
            existing.setShopifyId(dto.getShopifyId());
            existing.setActiveSubscription(
                    dto.getActiveSubscriptionsCount() != null &&
                            dto.getActiveSubscriptionsCount() > 0
            );
            customerDAO.upsert(existing);
        }

        return existing;
    }

    // ===================== SUBSCRIPTION =====================

    /**
     * Upsert subscription.
     * Customer SKAL være synced FØR denne metode kaldes.
     */
    public void syncSubscription(LoopSubscriptionDTO dto) {

        if (dto == null || dto.getLoopSubscriptionId() == null) return;

        Subscription existing =
                subscriptionDAO.findByLoopSubscriptionId(dto.getLoopSubscriptionId());

        Subscription sub = (existing != null) ? existing : new Subscription();

        sub.setLoopSubscriptionId(dto.getLoopSubscriptionId());

        if (dto.getStatus() != null) {
            sub.setStatus(SubscriptionStatus.valueOf(dto.getStatus()));
        }

        if (dto.getNextBillingDateEpoch() != null) {
            LocalDateTime next =
                    LocalDateTime.ofEpochSecond(dto.getNextBillingDateEpoch(), 0, ZoneOffset.UTC);
            sub.setNextBillingAt(next);
        }

        // 🔗 Sæt relation til customer (kun reference, ingen opdatering)
        if (dto.getCustomer() != null && dto.getCustomer().getShopifyId() != null) {
            Customer customer =
                    customerDAO.findByShopifyId(dto.getCustomer().getShopifyId());

            if (customer != null) {
                sub.setCustomer(customer);
            }
        }


        subscriptionDAO.upsert(sub);
    }
}
