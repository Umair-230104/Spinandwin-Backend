package app.unittest;

import app.config.HibernateConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class DaoTestBase
{

    protected static EntityManagerFactory emf;

    @BeforeAll
    static void beforeAll() {

//        System.setProperty(
//                "org.testcontainers.dockerclient.provider",
//                "org.testcontainers.dockerclient.DockerDesktopClientProviderStrategy"
//        );

        emf = HibernateConfig.getEntityManagerFactoryForTest();
    }


    @BeforeEach
    void beforeEach()
    {
        // (valgfrit) - hvis du vil logge noget, eller sikre at db er up
    }

    @AfterEach
    void afterEach()
    {
        // DB cleanup bliver gjort i de konkrete testklasser,
        // fordi rækkefølgen af DELETE afhænger af relationer.
    }

    @AfterAll
    static void afterAll()
    {
        if (emf != null) emf.close();
    }

    protected void cleanDb(String... deleteQueries)
    {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        for (String q : deleteQueries)
        {
            em.createQuery(q).executeUpdate();
        }
        em.getTransaction().commit();
        em.close();
    }
}
