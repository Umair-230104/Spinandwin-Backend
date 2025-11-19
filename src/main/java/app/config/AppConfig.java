package app.config;

import app.controllers.ExceptionController;
import app.exception.ApiException;
import app.routes.Routes;
import app.security.controllers.AccessController;
import app.security.controllers.SecurityController;
import app.security.enums.Role;
import app.security.exceptions.NotAuthorizedException;
import app.security.routes.SecurityRoutes;
import app.security.utils.Utils;
import app.util.ApiProps;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AppConfig
{
    private static final Routes routes = new Routes();
    private static final ExceptionController exceptionController = new ExceptionController();

    private static ObjectMapper jsonMapper = new Utils().getObjectMapper();
    private static SecurityController securityController = SecurityController.getInstance();
    private static AccessController accessController = new AccessController();
    private static Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static int count = 1;


    private static void configuration(JavalinConfig config)
    {
        // == Server ==
        config.router.contextPath = ApiProps.API_CONTEXT;

        // == Plugins ==
        config.bundledPlugins.enableRouteOverview("/routes", Role.ANYONE); // Enable route overview
        config.bundledPlugins.enableDevLogging(); // Enable development logging

        // == Routes ==
        config.router.apiBuilder(routes.getApiRoutes());

        // == Security Routes ==
        config.router.apiBuilder(SecurityRoutes.getSecurityRoutes());
        config.router.apiBuilder(SecurityRoutes.getSecuredRoutes());
    }

    // == Exception ==
    public static void exceptionHandler(Javalin app)
    {
        app.exception(ApiException.class, exceptionController::apiExceptionHandler);
        app.exception(Exception.class, exceptionController::exceptionHandler);
    }

    private static void applyCorsHeaders(Context ctx)
    {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
    }

    // == CORS Setup ==
    private static void configureCors(Javalin app)
    {
        app.before(AppConfig::applyCorsHeaders); // Apply headers to all requests

        app.options("/*", ctx ->
        {
            applyCorsHeaders(ctx); // Apply headers for preflight requests
            ctx.status(204); // No content response for OPTIONS requests
        });
    }

    // == Start server ==
    public static void startServer()
    {
        var app = Javalin.create(AppConfig::configuration);


        app.beforeMatched(accessController::accessHandler);
        app.after(AppConfig::afterRequest);
        app.exception(ApiException.class, AppConfig::apiExceptionHandler);
        app.exception(app.security.exceptions.ApiException.class, AppConfig::apiSecurityExceptionHandler);
        app.exception(NotAuthorizedException.class, AppConfig::apiNotAuthorizedExceptionHandler);
        app.exception(Exception.class, AppConfig::generalExceptionHandler);


        configureCors(app); // Configure CORS
        exceptionHandler(app); // Set exception handling

        // Add exception handlers
        exceptionHandler(app);
        app.error(404, ctx -> ctx.json("Not found")); // Handle 404 errors
        app.start(ApiProps.PORT);
    }

    // == Stop server ==
    public static void stopServer(Javalin app)
    {
        app.stop();
    }

    public static void afterRequest(Context ctx)
    {
        String requestInfo = ctx.req().getMethod() + " " + ctx.req().getRequestURI();
        logger.info(" Request {} - {} was handled with status code {}", count++, requestInfo, ctx.status());
    }

    public static void apiExceptionHandler(ApiException e, Context ctx)
    {
        ctx.status(e.getStatusCode());
        logger.warn("An API exception occurred: Code: {}, Message: {}", e.getStatusCode(), e.getMessage());
        ctx.json(Utils.convertToJsonMessage(ctx, "warning", e.getMessage()));
    }

    public static void apiSecurityExceptionHandler(app.security.exceptions.ApiException e, Context ctx)
    {
        ctx.status(e.getCode());
        logger.warn("A Security API exception occurred: Code: {}, Message: {}", e.getCode(), e.getMessage());
        ctx.json(Utils.convertToJsonMessage(ctx, "warning", e.getMessage()));
    }

    public static void apiNotAuthorizedExceptionHandler(NotAuthorizedException e, Context ctx)
    {
        ctx.status(e.getStatusCode());
        logger.warn("A Not authorized Security API exception occurred: Code: {}, Message: {}", e.getStatusCode(), e.getMessage());
        ctx.json(Utils.convertToJsonMessage(ctx, "warning", e.getMessage()));
    }

    private static void generalExceptionHandler(Exception e, Context ctx)
    {
        logger.error("An unhandled exception occurred", e.getMessage());
        ctx.json(Utils.convertToJsonMessage(ctx, "error", e.getMessage()));
    }
}
