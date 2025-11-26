package app;

import app.config.AppConfig;
import app.config.HibernateConfig;
import app.daos.LoopDAO;
import app.service.LoopService;
import app.service.LoopServiceTest; // eller LoopService hvis du omdøber den
import jakarta.persistence.EntityManagerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {

//        AppConfig.startServer();

//         === KUN når du vil sync'e fra Loop API til DB ===
//         Kommentér ind når du vil køre importen,
//         og kommentér ud igen bagefter så server-start ikke tager 100 år.

//         EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("spinandwindb");
//         LoopDAO loopDAO = new LoopDAO(emf);
//         LoopService loopService = new LoopService(loopDAO);
//
//         loopService.syncActiveLoopDataToDb();
    }
}
