package app.routes;

import app.config.HibernateConfig;
import app.controllers.LoopWebhookController;
import app.controllers.SpinAndWinController;
import app.daos.*;
import app.security.enums.Role;
import app.service.*;
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
    private final SpinResultDAO spinResultDAO = new SpinResultDAO(emf);

    // Service
    private final CustomerEligibilityService eligibilityService = new CustomerEligibilityService(customerDAO, subscriptionDAO, deliveryDAO);
    private final SpinService spinService = new SpinService(deliveryDAO, spinResultDAO, wheelSegmentDAO);

    // Controller (fælles controller)
    private final SpinAndWinController spinAndWinController = new SpinAndWinController(wheelSegmentDAO, eligibilityService, spinService);

    // Services
    private final LoopApiService loopApiService = new LoopApiService();
    private final LoopSyncService loopSyncService = new LoopSyncService(customerDAO, subscriptionDAO);
    private final LoopWebhookService loopWebhookService = new LoopWebhookService(loopApiService, loopSyncService);
    private final LoopWebhookController loopWebhookController = new LoopWebhookController(loopWebhookService);

    public EndpointGroup getSpinAndWinRoutes()
    {
        return () ->
        {
            get("/getallwheelsegments", spinAndWinController::getAllWheelSegments, Role.USER, Role.ADMIN, Role.ANYONE);
            put("/{id}/wheelsegment", spinAndWinController::updateWheelSegment, Role.USER, Role.ADMIN, Role.ANYONE);

            // eligibility
            post("/customer/check-eligibility", spinAndWinController::checkEligibility, Role.USER, Role.ADMIN, Role.ANYONE);

            // webhook (ingen auth til at starte med)
            post("/webhooks/loop", loopWebhookController::receive, Role.ANYONE);

            post("/spin", spinAndWinController::spin, Role.USER, Role.ADMIN, Role.ANYONE);
            get("/spin-result/{id}", spinAndWinController::getSpinResult, Role.ANYONE);

        };
    }
}
