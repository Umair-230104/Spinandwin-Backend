package app.apitest;

import app.Enums.DeliveryStatus;
import app.Enums.SegmentType;
import app.Enums.SubscriptionStatus;
import app.config.AppConfig;
import app.config.HibernateConfig;
import app.daos.*;
import app.dtos.EligibilityRequestDTO;
import app.dtos.EligibilityResponseDTO;
import app.entities.*;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SpinAndWinRouteTest
{
    private static EntityManagerFactory emf;
    private Long customerId;

    private static CustomerDAO customerDAO;
    private static SubscriptionDAO subscriptionDAO;
    private static DeliveryDAO deliveryDAO;
    private static WheelSegmentDAO wheelSegmentDAO;
    private static SpinResultDAO spinResultDAO;

    @BeforeAll
    static void setup() throws InterruptedException
    {
        HibernateConfig.enableTestMode();

        emf = HibernateConfig.getEntityManagerFactoryForTest(); // 🔥 DEN MANGLEDE

        AppConfig.startServer();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 2222;
        RestAssured.basePath = "/api/v1";

        Thread.sleep(300);
    }


    @BeforeEach
    void setUp()
    {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Customer customer = new Customer();
        customer.setEmail("test@mail.dk");
        customer.setPhone("12345678");
        customer.setActiveSubscription(true);
        em.persist(customer);

        em.flush(); // 🔥 vigtig
        customerId = customer.getId();

        Subscription sub = new Subscription();
        sub.setCustomer(customer);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setLoopSubscriptionId(1001L); // 🔥 KRITISK
        em.persist(sub);

        Delivery delivery = new Delivery();
        delivery.setSubscription(sub);
        delivery.setStatus(DeliveryStatus.SHIPPED);
        delivery.setShippedAt(LocalDateTime.now());
        em.persist(delivery);

        SpinWheel wheel = new SpinWheel();
        wheel.setName("Test Wheel");
        wheel.setActive(true);
        em.persist(wheel);

        WheelSegment segment = new WheelSegment();
        segment.setWheel(wheel); // 🔥 KRITISK
        segment.setPosition(1);
        segment.setTitle("Test Prize");
        segment.setType(SegmentType.PRIZE);
        segment.setActive(true);
        em.persist(segment);

        em.getTransaction().commit();
        em.close();
    }


    @AfterEach
    void tearDown()
    {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM SpinResult").executeUpdate();
        em.createQuery("DELETE FROM WheelSegment").executeUpdate();
        em.createQuery("DELETE FROM Delivery").executeUpdate();
        em.createQuery("DELETE FROM Subscription").executeUpdate();
        em.createQuery("DELETE FROM Customer").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    // ----------------------------------------------------
    // ROUTE TESTS
    // ----------------------------------------------------

    @Test
    void getAllWheelSegments_returns200()
    {
        given()
                .when()
                .get("/getallwheelsegments")
                .then()
                .statusCode(200)
                .body("$", not(empty()));
    }

    @Test
    void checkEligibility_returnsEligible()
    {
        EligibilityRequestDTO req = new EligibilityRequestDTO();
        req.setEmailOrPhone("test@mail.dk");


        EligibilityResponseDTO res =
                given()
                        .contentType("application/json")
                        .body(req)
                        .when()
                        .post("/customer/check-eligibility")
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(EligibilityResponseDTO.class);

        assertThat(res.isEligible(), is(true));
        assertThat(res.getReasonCode(), equalTo("OK"));
    }

    @Test
    void spin_returnsSpinResult()
    {
        Long spinResultId =
                given()
                        .contentType("application/json")
                        .body("{\"customerId\":" + customerId + "}")
                        .when()
                        .post("/spin")
                        .then()
                        .statusCode(200)
                        .body("spinResultId", notNullValue())
                        .extract()
                        .jsonPath()
                        .getLong("spinResultId");

        assertThat(spinResultId, notNullValue());
    }



    @Test
    void getSpinResult_returnsResult()
    {
        // først spin
        Long id =
                given()
                        .contentType("application/json")
                        .body("{\"customerId\":" + customerId + "}")
                        .when()
                        .post("/spin")
                        .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getLong("spinResultId");

        given()
                .when()
                .get("/spin-result/" + id)
                .then()
                .statusCode(200)
                .body("spinResultId", equalTo(id.intValue()));
    }

    @Test
    void loopWebhook_acceptsPayload()
    {
        given()
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/webhooks/loop")
                .then()
                .statusCode(anyOf(is(200), is(204)));
    }
}
