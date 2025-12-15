package app.routes;

import app.config.HibernateConfig;
import app.controllers.SpinAndWinController;
import app.daos.CustomerDAO;
import app.daos.DeliveryDAO;
import app.daos.SubscriptionDAO;
import app.daos.WheelSegmentDAO;
import app.security.enums.Role;
import app.service.CustomerEligibilityService;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;

public class SpinAndWinRoutes
{

    private final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("spinandwindb");

    // DAO'er
    private final WheelSegmentDAO wheelSegmentDAO = new WheelSegmentDAO(emf);
    private final CustomerDAO customerDAO = new CustomerDAO(emf);
    private final SubscriptionDAO subscriptionDAO = new SubscriptionDAO(emf);
    private final DeliveryDAO deliveryDAO = new DeliveryDAO(emf);

    // Service
    private final CustomerEligibilityService eligibilityService = new CustomerEligibilityService(customerDAO, subscriptionDAO, deliveryDAO);

    // Controller (fælles controller)
    private final SpinAndWinController spinAndWinController = new SpinAndWinController(wheelSegmentDAO, eligibilityService);

    public EndpointGroup getSpinAndWinRoutes()
    {
        return () ->
        {
            get("/getallwheelsegments", spinAndWinController::getAllWheelSegments, Role.USER, Role.ADMIN, Role.ANYONE);
            put("/{id}/wheelsegment", spinAndWinController::updateWheelSegment, Role.USER, Role.ADMIN, Role.ANYONE);

            // ✅ NY ROUTE: eligibility
            post("/customer/check-eligibility", spinAndWinController::checkEligibility, Role.USER, Role.ADMIN, Role.ANYONE);
        };
    }
}
