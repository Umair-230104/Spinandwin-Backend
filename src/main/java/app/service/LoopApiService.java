package app.service;

import app.dtos.LoopCustomerDTO;
import app.dtos.LoopCustomerSingleResponseDTO;
import app.dtos.LoopSubscriptionDTO;
import app.dtos.LoopSubscriptionSingleResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LoopApiService
{

    private static final String BASE_URL = "https://api.loopsubscriptions.com/admin/2023-10";
    private static final String TOKEN = System.getenv("LOOP_ADMIN_TOKEN");

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();


    // ===================== PUBLIC API =====================
    public LoopSubscriptionDTO fetchSubscriptionById(Long subscriptionId)
            throws IOException, InterruptedException, URISyntaxException
    {

        String url = BASE_URL + "/subscription/" + subscriptionId;
        HttpResponse<String> res = send(buildGet(url));

        LoopSubscriptionSingleResponseDTO response =
                parseIf200Dto(res, LoopSubscriptionSingleResponseDTO.class);

        if (response == null)
        {
            return null;
        }

        return response.getData();
    }


    public LoopCustomerDTO fetchCustomerByShopifyId(Long customerShopifyId)
            throws IOException, InterruptedException, URISyntaxException
    {

        String url = BASE_URL + "/customer/" + customerShopifyId; // <-- vigtig
        System.out.println("➡️ GET " + url);

        HttpResponse<String> res = send(buildGet(url));

        System.out.println("⬅️ STATUS: " + res.statusCode());
        if (res.statusCode() != 200)
        {
            System.out.println("⬅️ BODY:\n" + res.body());
            return null;
        }

        LoopCustomerSingleResponseDTO response =
                mapper.readValue(res.body(), LoopCustomerSingleResponseDTO.class);

        return response == null ? null : response.getData();
    }


    // ===================== HTTP HELPERS =====================
    private HttpRequest buildGet(String url) throws URISyntaxException
    {
        return HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("accept", "application/json")
                .header("X-Loop-Token", TOKEN)
                .GET()
                .build();
    }

    private HttpResponse<String> send(HttpRequest req)
            throws IOException, InterruptedException
    {
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private <T> T parseIf200Dto(HttpResponse<String> res, Class<T> clazz)
            throws IOException
    {

        if (res != null && res.statusCode() == 200)
        {
            return mapper.readValue(res.body(), clazz);
        }
        return null;
    }
}
