package app.unittest;

import app.Enums.SubscriptionStatus;
import app.daos.CustomerDAO;
import app.daos.DeliveryDAO;
import app.daos.SubscriptionDAO;
import app.dtos.EligibilityResponseDTO;
import app.entities.Customer;
import app.service.CustomerEligibilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerEligibilityServiceTest {

    private CustomerDAO customerDAO;
    private SubscriptionDAO subscriptionDAO;
    private DeliveryDAO deliveryDAO;

    private CustomerEligibilityService service;

    @BeforeEach
    void setup() {
        customerDAO = mock(CustomerDAO.class);
        subscriptionDAO = mock(SubscriptionDAO.class);
        deliveryDAO = mock(DeliveryDAO.class);

        service = new CustomerEligibilityService(
                customerDAO,
                subscriptionDAO,
                deliveryDAO
        );
    }

    @Test
    void checkEligibility_customerNotFound() {

        when(customerDAO.findSingleByEmailOrPhone("test@mail.dk"))
                .thenReturn(null);

        EligibilityResponseDTO result =
                service.checkEligibility("test@mail.dk");

        assertFalse(result.isEligible());
        assertEquals("NOT_FOUND", result.getReasonCode());
    }

    @Test
    void checkEligibility_noActiveSubscription() {

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setEmail("test@mail.dk");
        customer.setPhone("12345678");

        when(customerDAO.findSingleByEmailOrPhone(any()))
                .thenReturn(customer);

        when(subscriptionDAO.existsByCustomerIdAndStatus(
                1L, SubscriptionStatus.ACTIVE))
                .thenReturn(false);

        EligibilityResponseDTO result =
                service.checkEligibility("test@mail.dk");

        assertFalse(result.isEligible());
        assertEquals("NO_ACTIVE_SUB", result.getReasonCode());
    }

    @Test
    void checkEligibility_noDeliveredDelivery() {

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setEmail("test@mail.dk");
        customer.setPhone("12345678");

        when(customerDAO.findSingleByEmailOrPhone(any()))
                .thenReturn(customer);

        when(subscriptionDAO.existsByCustomerIdAndStatus(
                1L, SubscriptionStatus.ACTIVE))
                .thenReturn(true);

        when(deliveryDAO.existsDeliveredByCustomerId(1L))
                .thenReturn(false);

        EligibilityResponseDTO result =
                service.checkEligibility("test@mail.dk");

        assertFalse(result.isEligible());
        assertEquals("NO_DELIVERED_DELIVERY", result.getReasonCode());
    }

    @Test
    void checkEligibility_ok() {

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setEmail("test@mail.dk");
        customer.setPhone("12345678");

        when(customerDAO.findSingleByEmailOrPhone(any()))
                .thenReturn(customer);

        when(subscriptionDAO.existsByCustomerIdAndStatus(
                1L, SubscriptionStatus.ACTIVE))
                .thenReturn(true);

        when(deliveryDAO.existsDeliveredByCustomerId(1L))
                .thenReturn(true);

        EligibilityResponseDTO result =
                service.checkEligibility("test@mail.dk");

        assertTrue(result.isEligible());
        assertEquals("OK", result.getReasonCode());
        assertEquals("Test User", result.getCustomerName());
    }
}
