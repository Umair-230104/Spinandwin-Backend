package app.config;


import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;

/**
 * Purpose: This class is used to configure Hibernate and create an EntityManagerFactory.
 * Author: Thomas Hartmann
 */

public class HibernateConfig
{
    private static EntityManagerFactory emf;

    private static EntityManagerFactory emfTest;

    private static boolean isTest = false;

    public static void enableTestMode() {
        isTest = true;
    }

    public static EntityManagerFactory getEntityManagerFactory(String DBName)
    {
        if (isTest) {
            return getEntityManagerFactoryForTest();
        }
        if (emf == null) emf = createEMF(false, DBName);
        return emf;
    }


    public static EntityManagerFactory getEntityManagerFactoryForTest()
    {
        if (emfTest == null) emfTest = createEMF(true, "");
        return emfTest;
    }

    // TODO: IMPORTANT: Add Entity classes here for them to be registered with Hibernate
    private static void getAnnotationConfiguration(Configuration configuration)
    {
        configuration.addAnnotatedClass(app.security.entities.User.class);
        configuration.addAnnotatedClass(app.security.entities.Role.class);
        configuration.addAnnotatedClass(app.entities.Customer.class);
        configuration.addAnnotatedClass(app.entities.Delivery.class);
        configuration.addAnnotatedClass(app.entities.SpinResult.class);
        configuration.addAnnotatedClass(app.entities.SpinWheel.class);
        configuration.addAnnotatedClass(app.entities.Subscription.class);
        configuration.addAnnotatedClass(app.entities.WheelSegment.class);
    }

    private static EntityManagerFactory createEMF(boolean forTest, String DBName)
    {
        try
        {
            Configuration configuration = new Configuration();
            Properties props = new Properties();
            // Set the properties
            setBaseProperties(props);
            if (forTest)
            {
                props = setTestProperties(props);
            } else if (System.getenv("DEPLOYED") != null)
            {
                setDeployedProperties(props, DBName);
            } else
            {
                props = setDevProperties(props, DBName);
            }
            configuration.setProperties(props);
            getAnnotationConfiguration(configuration);

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
            SessionFactory sf = configuration.buildSessionFactory(serviceRegistry);
            EntityManagerFactory emf = sf.unwrap(EntityManagerFactory.class);
            return emf;
        } catch (Throwable ex)
        {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static String getDBName()
    {

//        return Utils.getPropertyValue("db.name", "properties-from-pom.properties");
        return "emp_ex";
    }

    private static Properties setBaseProperties(Properties props)
    {
        // props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        props.put("hibernate.hbm2ddl.auto", "update"); //Vi skal lige ændre til update igen
        props.put("hibernate.current_session_context_class", "thread");
        props.put("hibernate.show_sql", "true");
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.use_sql_comments", "true");
        return props;
    }

    private static Properties setDeployedProperties(Properties props, String DBName)
    {
        props.setProperty("hibernate.connection.url", System.getenv("CONNECTION_STR") + DBName);
        props.setProperty("hibernate.connection.username", System.getenv("DB_USERNAME"));
        props.setProperty("hibernate.connection.password", System.getenv("DB_PASSWORD"));
        return props;
    }

    private static Properties setDevProperties(Properties props, String DBName)
    {
        props.put("hibernate.connection.url", "jdbc:postgresql://localhost:5432/" + DBName);
        // postgres som kode, hvis ikke man har ændret koden
        props.put("hibernate.connection.username", "postgres");
        props.put("hibernate.connection.password", "postgres");
        return props;
    }

    // This method is used to set the properties for the development environment.
//    private static Properties setDevProperties(Properties props, String DBName)
//    {
//        props.put("hibernate.connection.url", "jdbc:postgresql://165.227.128.237/" + DBName);
//        // postgres som kode, hvis ikke man har ændret koden
//        props.put("hibernate.connection.username", "postgres");
//        props.put("hibernate.connection.password", "umair04");
//        return props;
//    }

//    private static Properties setTestProperties(Properties props)
//    {
//        // props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
//        props.put("hibernate.connection.driver_class", "org.testcontainers.jdbc.ContainerDatabaseDriver");
//        props.put("hibernate.connection.url", "jdbc:tc:postgresql:15.3-alpine3.18:///test_db");
//        props.put("hibernate.connection.username", "postgres");
//        props.put("hibernate.connection.password", "postgres");
//        props.put("hibernate.archive.autodetection", "class");
//        props.put("hibernate.show_sql", "true");
//        props.put("hibernate.hbm2ddl.auto", "create-drop");
//        return props;
//    }

    private static Properties setTestProperties(Properties props)
    {
        props.put("hibernate.connection.driver_class", "org.h2.Driver");
        props.put("hibernate.connection.url", "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        props.put("hibernate.connection.username", "sa");
        props.put("hibernate.connection.password", "");
        props.put("hibernate.hbm2ddl.auto", "create-drop");

        props.put("hibernate.show_sql", "true");
        props.put("hibernate.format_sql", "true");

        return props;
    }

}
