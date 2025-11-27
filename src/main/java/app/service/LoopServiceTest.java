package app.service;

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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LoopServiceTest
{

    // --- KONFIG ---
    private static final ZoneId ZONE = ZoneId.of("Europe/Copenhagen");
    private static final String BASE_URL = "https://api.loopsubscriptions.com/admin/2023-10";
    private static final String TOKEN = System.getenv("LOOP_ADMIN_TOKEN");

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // ========= HJÆLPEMETODER =========

    private static String epochToCopenhagen(String epochSeconds)
    {
        try
        {
            long sec = Long.parseLong(epochSeconds);
            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z", Locale.forLanguageTag("da-DK")).withZone(ZONE).format(Instant.ofEpochSecond(sec));
        } catch (Exception e)
        {
            return "?";
        }
    }

    private static String safe(String s)
    {
        return s != null ? s : "";
    }

    private HttpRequest buildGet(String fullUrl) throws URISyntaxException
    {
        return HttpRequest.newBuilder().uri(new URI(fullUrl)).header("accept", "application/json").header("X-Loop-Token", TOKEN).GET().build();
    }

    /**
     * Sender GET med retry/backoff på 429 og 5xx.
     */
    private HttpResponse<String> sendWithRetry(HttpRequest req) throws IOException, InterruptedException
    {
        int maxRetries = 6;
        long backoffMs = 400;
        for (int attempt = 0; attempt <= maxRetries; attempt++)
        {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            int code = res.statusCode();
            if (code < 400) return res;

            if (code == 429 || (code >= 500 && code < 600))
            {
                String retryAfter = res.headers().firstValue("Retry-After").orElse(null);
                long waitMs = retryAfter != null ? (Long.parseLong(retryAfter) * 1000L) : backoffMs;
                if (attempt == maxRetries) return res;
                Thread.sleep(waitMs);
                backoffMs = Math.min(backoffMs * 2, 5000);
                continue;
            }
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

    // ========= KALD TIL LOOP API =========

    /**
     * LIST: subscriptions globalt (valgfrit filter på status) + pagination (offset)
     */
    public LoopSubscriptionsResponseDTO getSubscriptions(String status, int limit, int offset) throws IOException, InterruptedException, URISyntaxException
    {

        StringBuilder sb = new StringBuilder(BASE_URL).append("/subscription?limit=").append(limit).append("&offset=").append(offset);

        if (status != null && !status.isBlank())
        {
            sb.append("&status=").append(URLEncoder.encode(status, StandardCharsets.UTF_8));
        }

        HttpResponse<String> res = sendWithRetry(buildGet(sb.toString()));
        return parseIf200Dto(res, LoopSubscriptionsResponseDTO.class);
    }

    /**
     * LIST: customers med pagination via pageNo + limit
     */
    public LoopCustomersResponseDTO getCustomers(int pageNo, int limit) throws IOException, InterruptedException, URISyntaxException
    {

        String fullUrl = BASE_URL + "/customer?pageNo=" + pageNo + "&limit=" + limit;
        HttpResponse<String> res = sendWithRetry(buildGet(fullUrl));
        return parseIf200Dto(res, LoopCustomersResponseDTO.class);
    }

    // ========= RAPPORT 1: Aktive subscriptions =========

    public void printActiveSubscriptionsOnly()
    {
        try
        {
            final int PAGE_LIMIT = 100;
            int offset = 0;

            System.out.println("=== Aktive subscriptions (KUN subscriptions) ===\n");

            while (true)
            {
                System.out.println("[printActiveSubscriptionsOnly] Henter subs, offset=" + offset);
                LoopSubscriptionsResponseDTO subsPage = getSubscriptions("ACTIVE", PAGE_LIMIT, offset);

                if (subsPage == null || subsPage.getData() == null || subsPage.getData().isEmpty())
                {
                    if (offset == 0)
                    {
                        System.out.println("Ingen aktive subscriptions fundet.");
                    }
                    break;
                }

                System.out.println("Fik " + subsPage.getData().size() + " aktive subs på denne side.\n");

                for (LoopSubscriptionDTO s : subsPage.getData())
                {
                    Long loopSubId = s.getLoopSubscriptionId();
                    String status = s.getStatus() != null ? s.getStatus() : "N/A";
                    Long nextEpoch = s.getNextBillingDateEpoch() != null ? s.getNextBillingDateEpoch() : 0L;

                    System.out.println("Subscription Loop ID : " + loopSubId);
                    System.out.println("Status               : " + status);

                    if (nextEpoch > 0)
                    {
                        String epochStr = Long.toString(nextEpoch);
                        System.out.println("nextBilling          : epoch=" + epochStr + " | " + epochToCopenhagen(epochStr));
                    } else
                    {
                        System.out.println("nextBilling          : -");
                    }

                    System.out.println();
                }

                if (subsPage.getPageInfo() == null || !subsPage.getPageInfo().isHasNextPage())
                {
                    System.out.println("[printActiveSubscriptionsOnly] Ingen flere subscription-sider. Stopper.\n");
                    break;
                }

                offset += PAGE_LIMIT;
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // ========= RAPPORT 2: Kunder med min X aktive subs =========

    public void printCustomersWithMinActiveSubs(int minActiveSubs)
    {
        try
        {
            final int PAGE_LIMIT = 500;
            int pageNo = 1;
            long totalMatched = 0;

            System.out.println("=== Kunder med mindst " + minActiveSubs + " aktiv(e) subscription(s) ===\n");

            while (true)
            {
                System.out.println("[printCustomersWithMinActiveSubs] Henter customers, pageNo=" + pageNo);
                LoopCustomersResponseDTO page = getCustomers(pageNo, PAGE_LIMIT);

                if (page == null || page.getData() == null || page.getData().isEmpty())
                {
                    System.out.println("Tom / ingen data på pageNo=" + pageNo + " → stopper.");
                    break;
                }

                for (LoopCustomerDTO c : page.getData())
                {
                    int activeCount = c.getActiveSubscriptionsCount() != null ? c.getActiveSubscriptionsCount() : 0;

                    if (activeCount >= minActiveSubs)
                    {
                        totalMatched++;
                        System.out.println("Customer Loop ID  : " + c.getLoopCustomerId());
                        System.out.println("Navn              : " + safe(c.getFirstName()) + " " + safe(c.getLastName()));
                        System.out.println("Email             : " + safe(c.getEmail()));
                        System.out.println("Active subs count : " + activeCount);
                        System.out.println();
                    }
                }

                if (page.getPageInfo() == null || !page.getPageInfo().isHasNextPage())
                {
                    System.out.println("pageInfo.hasNextPage = false → ingen flere sider.");
                    break;
                }

                pageNo++;

                if (pageNo > 200)
                {
                    System.out.println("pageNo > 200 → failsafe stop.");
                    break;
                }
            }

            System.out.println("=== SUMMARY ===");
            System.out.println("Kunder med min " + minActiveSubs + " aktive subs: " + totalMatched);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // ========= RAPPORT 3: Kombineret – som du bad om =========

    /**
     * Henter OG printer:
     * 1) Alle aktive subscriptions
     * 2) Alle kunder med mindst 'minActiveSubs' aktive abonnementer
     */
    public void printActiveSubsAndCustomers(int minActiveSubs)
    {
        System.out.println("=== START: samlet Loop-rapport ===\n");

        // Først alle aktive subs
        printActiveSubscriptionsOnly();

        // Så kunder med mindst X aktive subs
        printCustomersWithMinActiveSubs(minActiveSubs);

        System.out.println("\n=== SLUT: samlet Loop-rapport ===");
    }
}
