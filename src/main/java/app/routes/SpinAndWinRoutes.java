package app.routes;

import app.config.HibernateConfig;
import app.controllers.SpinAndWinController;
import app.daos.WheelSegmentDAO;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.put;

public class SpinAndWinRoutes
{
    private final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("spinandwindb");
    private final WheelSegmentDAO wheelSegmentDAO = new WheelSegmentDAO(emf);
    private final SpinAndWinController spinAndWinController = new SpinAndWinController(wheelSegmentDAO);

    public EndpointGroup getSpinAndWinRoutes()
    {
        return () ->
        {
            get("/getallwheelsegments", spinAndWinController::getAllWheelSegments, Role.USER, Role.ADMIN, Role.ANYONE);
            put("/{id}/wheelsegment", spinAndWinController::updateWheelSegment, Role.USER, Role.ADMIN, Role.ANYONE);
        };
    }
}
