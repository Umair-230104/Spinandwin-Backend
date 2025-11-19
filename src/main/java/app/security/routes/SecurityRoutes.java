package app.security.routes;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.security.utils.Utils;
import app.security.controllers.SecurityController;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * Purpose: To handle security in the API
 *  Author: Thomas Hartmann
 */
public class SecurityRoutes {
    private static ObjectMapper jsonMapper = new Utils().getObjectMapper();
    private static SecurityController securityController = SecurityController.getInstance();
    public static EndpointGroup getSecurityRoutes() {
        return () -> {
            path("/auth", () -> {
                get("/healthcheck", securityController::healthCheck, Role.ANYONE);
                get("/test", ctx -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from Open Deployment")), Role.ANYONE);
                post("/login", securityController.login(), Role.ANYONE);
                post("/register", securityController.register(), Role.ANYONE);
                post("/user/addrole", securityController.addRole(), Role.ADMIN, Role.USER);

                // Admin-only endpoints
                get("/users", securityController.getAllUsers(), Role.ADMIN); // GET all users
                put("/user", securityController.updateUser(), Role.ADMIN); // Update password
                delete("/user/{username}", securityController.deleteUser(), Role.ADMIN);
            });
        };
    }

    public static EndpointGroup getSecuredRoutes(){
        return ()->{
            path("/protected", ()->{
                get("/user_demo", (ctx)->ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from USER Protected")), Role.USER);
                get("/admin_demo", (ctx)->ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from ADMIN Protected")), Role.ADMIN);
            });
        };
    }
}
