package app.service;

import app.Enums.SubscriptionStatus;
import app.daos.CustomerDAO;
import app.daos.DeliveryDAO;
import app.daos.SubscriptionDAO;
import app.dtos.EligibilityResponseDTO;
import app.entities.Customer;

public class CustomerEligibilityService
{

    private final CustomerDAO customerDAO;
    private final SubscriptionDAO subscriptionDAO;
    private final DeliveryDAO deliveryDAO;

    public CustomerEligibilityService(CustomerDAO customerDAO,
                                      SubscriptionDAO subscriptionDAO,
                                      DeliveryDAO deliveryDAO)
    {
        this.customerDAO = customerDAO;
        this.subscriptionDAO = subscriptionDAO;
        this.deliveryDAO = deliveryDAO;
    }

    /**
     * Tjekker om en kunde er berettiget til at spinne:
     * 1) Findes kunden (email eller telefon)?
     * 2) Har kunden et aktivt abonnement?
     * 3) Har kunden mindst én levering med status SHIPPED?
     */
    public EligibilityResponseDTO checkEligibility(String emailOrPhone)
    {

        // 1) Find kunden via email eller telefon
        Customer customer = customerDAO.findSingleByEmailOrPhone(emailOrPhone);

        if (customer == null)
        {
            return new EligibilityResponseDTO(
                    false,
                    "NOT_FOUND",
                    null,
                    null,
                    null
            );
        }

        // 2) Tjek om kunden har et aktivt abonnement
        boolean hasActiveSubscription =
                subscriptionDAO.existsByCustomerIdAndStatus(customer.getId(), SubscriptionStatus.ACTIVE);

        if (!hasActiveSubscription)
        {
            return new EligibilityResponseDTO(
                    false,
                    "NO_ACTIVE_SUB",
                    customer.getFirstName() + " " + customer.getLastName(),
                    customer.getEmail(),
                    customer.getPhone()
            );
        }

        // 3) Tjek om kunden har mindst én leveret levering (SHIPPED)
        boolean hasDeliveredDelivery =
                deliveryDAO.existsDeliveredByCustomerId(customer.getId());

        if (!hasDeliveredDelivery)
        {
            return new EligibilityResponseDTO(
                    false,
                    "NO_DELIVERED_DELIVERY",
                    customer.getFirstName() + " " + customer.getLastName(),
                    customer.getEmail(),
                    customer.getPhone()
            );
        }

        // 4) Hvis vi kommer hertil, er kunden berettiget til at spinne 🎉
        return new EligibilityResponseDTO(
                true,
                "OK",
                customer.getFirstName() + " " + customer.getLastName(),
                customer.getEmail(),
                customer.getPhone()
        );
    }
}
