package app.service;

import app.daos.LoopDAO;
import app.dtos.LoopCustomerDTO;
import app.dtos.LoopCustomersResponseDTO;
import app.dtos.LoopSubscriptionDTO;
import app.dtos.LoopSubscriptionsResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LoopService
{

    private static final String BASE_URL = "https://api.loopsubscriptions.com/admin/2023-10";
    private static final String TOKEN = System.getenv("LOOP_ADMIN_TOKEN");

    private final HttpClient client;
    private final ObjectMapper mapper;
    private final LoopDAO loopDAO;

    public LoopService(LoopDAO loopDAO)
    {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.loopDAO = loopDAO;
    }

    /**
     * Hovedmetode – svarer til getPokemonsToDB():
     * 1) Hent alle aktive subscriptions fra Loop
     * 2) Hent kun de kunder, der matcher disse subscriptions
     * 3) Gem det hele i databasen via LoopDAO
     */
    public void syncActiveLoopDataToDb() throws IOException, InterruptedException, URISyntaxException
    {
        // 1) Hent alle aktive subscriptions
        List<LoopSubscriptionDTO> activeSubs = fetchAllActiveSubscriptions();

        if (activeSubs.isEmpty())
        {
            // intet at gemme
            return;
        }

        // 2) Hent kunderne, som har de subscriptions
        Map<Long, LoopCustomerDTO> customersById = fetchCustomersForSubscriptions(activeSubs);

        // 3) Gem begge dele i DB
        loopDAO.saveLoopDataToDb(activeSubs, customersById);
    }

    // ===================== FETCH HELPERS =====================

    private List<LoopSubscriptionDTO> fetchAllActiveSubscriptions()
            throws IOException, InterruptedException, URISyntaxException
    {

        final int PAGE_LIMIT = 100;
        int offset = 0;
        List<LoopSubscriptionDTO> all = new ArrayList<>();

        while (true)
        {
            LoopSubscriptionsResponseDTO page = getSubscriptionsPage("ACTIVE", PAGE_LIMIT, offset);

            if (page == null || page.getData() == null || page.getData().isEmpty())
            {
                break;
            }

            all.addAll(page.getData());

            if (page.getPageInfo() == null || !page.getPageInfo().isHasNextPage())
            {
                break;
            }

            offset += PAGE_LIMIT;
        }

        return all;
    }

    /**
     * Henter kunder via /customer?pageNo=..&limit=.. og filtrerer så vi kun
     * beholder dem, der matcher de loopCustomerId’er, vi har i subscriptions-listen.
     */
    private Map<Long, LoopCustomerDTO> fetchCustomersForSubscriptions(List<LoopSubscriptionDTO> subs)
            throws IOException, InterruptedException, URISyntaxException
    {

        // Find alle unikke Loop-customer-id'er fra subscriptions
        Set<Long> neededCustomerIds = new HashSet<>();
        for (LoopSubscriptionDTO s : subs)
        {
            if (s.getCustomer() != null && s.getCustomer().getId() != null)
            {
                neededCustomerIds.add(s.getCustomer().getId());
            }
        }

        Map<Long, LoopCustomerDTO> result = new HashMap<>();
        if (neededCustomerIds.isEmpty())
        {
            return result;
        }

        final int PAGE_LIMIT = 500;
        int pageNo = 1;

        while (true)
        {
            LoopCustomersResponseDTO page = getCustomersPage(pageNo, PAGE_LIMIT);

            if (page == null || page.getData() == null || page.getData().isEmpty())
            {
                break;
            }

            // kun gem kunder, vi faktisk har brug for
            page.getData().stream()
                    .filter(c -> neededCustomerIds.contains(c.getLoopCustomerId()))
                    .forEach(c -> result.put(c.getLoopCustomerId(), c));

            if (page.getPageInfo() == null || !page.getPageInfo().isHasNextPage())
            {
                break;
            }

            pageNo++;

            // failsafe
            if (pageNo > 200)
            { // 200 * 500 = 100.000 kunder
                break;
            }
        }

        return result;
    }

    // ===================== HTTP + PARSE =====================

    private LoopSubscriptionsResponseDTO getSubscriptionsPage(String status, int limit, int offset)
            throws IOException, InterruptedException, URISyntaxException
    {

        StringBuilder sb = new StringBuilder(BASE_URL)
                .append("/subscription?limit=").append(limit)
                .append("&offset=").append(offset);

        if (status != null && !status.isBlank())
        {
            sb.append("&status=").append(URLEncoder.encode(status, StandardCharsets.UTF_8));
        }

        HttpResponse<String> res = sendWithRetry(buildGet(sb.toString()));
        return parseIf200Dto(res, LoopSubscriptionsResponseDTO.class);
    }

    private LoopCustomersResponseDTO getCustomersPage(int pageNo, int limit)
            throws IOException, InterruptedException, URISyntaxException
    {

        String fullUrl = BASE_URL + "/customer?pageNo=" + pageNo + "&limit=" + limit;
        HttpResponse<String> res = sendWithRetry(buildGet(fullUrl));
        return parseIf200Dto(res, LoopCustomersResponseDTO.class);
    }

    private HttpRequest buildGet(String fullUrl) throws URISyntaxException
    {
        return HttpRequest.newBuilder()
                .uri(new URI(fullUrl))
                .header("accept", "application/json")
                .header("X-Loop-Token", TOKEN)
                .GET()
                .build();
    }

    private HttpResponse<String> sendWithRetry(HttpRequest req) throws IOException, InterruptedException
    {
        int maxRetries = 6;
        long backoffMs = 400;

        for (int attempt = 0; attempt <= maxRetries; attempt++)
        {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            int code = res.statusCode();

            if (code < 400)
            {
                return res;
            }

            // 429 / 5xx → retry med backoff
            if (code == 429 || (code >= 500 && code < 600))
            {
                String retryAfter = res.headers().firstValue("Retry-After").orElse(null);
                long waitMs = retryAfter != null ? (Long.parseLong(retryAfter) * 1000L) : backoffMs;

                if (attempt == maxRetries)
                {
                    return res;
                }

                Thread.sleep(waitMs);
                backoffMs = Math.min(backoffMs * 2, 5000);
                continue;
            }

            // andre fejl → returnér bare
            return res;
        }

        return null;
    }

    private <T> T parseIf200Dto(HttpResponse<String> res, Class<T> clazz) throws IOException
    {
        if (res != null && res.statusCode() == 200)
        {
            return mapper.readValue(res.body(), clazz);
        }
        return null;
    }
}
