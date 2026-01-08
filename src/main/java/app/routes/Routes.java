package app.routes;


import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes
{
    private final SpinAndWinRoutes spinAndWinRoute = new SpinAndWinRoutes();

    public EndpointGroup getApiRoutes()
    {
        return () ->
        {
            path("/", spinAndWinRoute.getSpinAndWinRoutes());
        };
    }
}
