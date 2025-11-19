package app.service;

import app.dtos.LoopBillingPolicyDTO;
import app.dtos.LoopDeliveryPolicyDTO;
import app.dtos.LoopShippingAddressDTO;
import app.dtos.LoopSubscriptionDTO;
import app.dtos.LoopSubscriptionsResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LoopService {

    // --- KONFIG (flyttet fra Main) ---
    private static final ZoneId ZONE = ZoneId.of("Europe/Copenhagen");
    private static final int UPCOMING_COUNT = 10; // hvor mange fremtidige leveringer vil du vise?

    private static final String BASE_URL = "https://api.loopsubscriptions.com/admin/2023-10";
    private static final String TOKEN   = System.getenv("LOOP_ADMIN_TOKEN");

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // ========== OFFENTLIG "ENTRYPOINT" FRA MAIN ==========
    /**
     * Printer rapport over aktive abonnementer, nu baseret på Loop-DTO'er
     */
    public void printActiveSubscriptionsReport() {
        try {
            final int PAGE_LIMIT = 100; // stort limit = færre kald
            int offset = 0;

            long totalActiveSubs = 0;
            long uniqueCustomersWithActiveSubs = 0;

            System.out.println("=== Printer KUN kunder med AKTIVE abonnementer ===");

            HashSet<String> seenCustomers = new HashSet<>();

            while (true) {
                LoopSubscriptionsResponseDTO subsPage = getSubscriptions("ACTIVE", PAGE_LIMIT, offset);

                if (subsPage == null || subsPage.getData() == null || subsPage.getData().isEmpty()) {
                    if (offset == 0) {
                        System.out.println("Stopper: ingen/ugyldig subscriptions data på offset=" + offset);
                    } else {
                        System.out.println("Ingen aktive subscriptions på denne side. Stopper.");
                    }
                    break;
                }

                int countOnPage = subsPage.getData().size();
                System.out.println("\n— ACTIVE subscriptions page offset=" + offset + " (count=" + countOnPage + ") —");

                for (LoopSubscriptionDTO s : subsPage.getData()) {
                    totalActiveSubs++;

                    String loopId = String.valueOf(s.getId());
                    String status = s.getStatus() != null ? s.getStatus() : "N/A";

                    String custId = s.getCustomer() != null
                            ? String.valueOf(s.getCustomer().getId())
                            : "UNKNOWN";

                    if (seenCustomers.add(custId)) {
                        uniqueCustomersWithActiveSubs++;
                    }

                    System.out.println("■ CustomerId=" + custId);
                    System.out.println("  Subscription (Loop ID)       : " + loopId);
                    System.out.println("  Status                       : " + status);
                    System.out.println("  lastPaymentStatus            : " +
                            (s.getLastPaymentStatus() != null ? s.getLastPaymentStatus() : "-"));

                    // Næste fakturering / levering
                    long nextEpoch = s.getNextBillingDateEpoch();
                    if (nextEpoch > 0) {
                        String nextEpochStr = Long.toString(nextEpoch);
                        System.out.println("  nextBilling: epoch=" + nextEpochStr +
                                " | " + epochToCopenhagen(nextEpochStr));

                        // Policies og kommende leveringer
                        LoopDeliveryPolicyDTO deliveryPolicy = s.getDeliveryPolicy();
                        LoopBillingPolicyDTO billingPolicy = s.getBillingPolicy();

                        printPolicies(billingPolicy, deliveryPolicy);

                        if (deliveryPolicy != null) {
                            String interval = deliveryPolicy.getInterval();
                            int intervalCount = deliveryPolicy.getIntervalCount();

                            List<ZonedDateTime> upcoming = computeUpcomingDeliveries(
                                    nextEpochStr,
                                    interval,
                                    intervalCount,
                                    UPCOMING_COUNT
                            );

                            if (!upcoming.isEmpty()) {
                                System.out.println("    [Upcoming deliveries] (" + interval + " x " + intervalCount + ")");
                                DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                                        "yyyy-MM-dd HH:mm:ss z",
                                        Locale.forLanguageTag("da-DK")
                                );
                                for (int i = 0; i < upcoming.size(); i++) {
                                    System.out.println("      #" + (i + 1) + ": " + upcoming.get(i).format(fmt));
                                }
                            }
                        } else {
                            System.out.println("    [Upcoming deliveries] - (ingen deliveryPolicy)");
                        }

                    } else {
                        System.out.println("  nextBilling: -");
                    }

                    // Shipping address (fra DTO)
                    printShipping(s.getShippingAddress());

                    System.out.println(); // spacer
                }

                // Pagination – bruger stadig pageInfo fra JSON via raw-kald, hvis du vil
                // Men her har vi valgt ikke at parse pageInfo til DTO -> du kan evt. tilføje det senere

                // For nu: bryd efter første side
                // Hvis du vil have pagination rigtigt, kan du tilføje pageInfo til LoopSubscriptionsResponseDTO
                break;
            }

            System.out.println("\n=== SUMMARY (aktive) ===");
            System.out.println("Aktive subscriptions i alt     : " + totalActiveSubs);
            System.out.println("Unikke kunder med aktive subs  : " + uniqueCustomersWithActiveSubs);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== HJÆLPEMETODER ==========

    // Hjælpemetode: konverter epoch sekunder -> lokal tid (København)
    private static String epochToCopenhagen(String epochSeconds) {
        try {
            long sec = Long.parseLong(epochSeconds);
            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z", Locale.forLanguageTag("da-DK"))
                    .withZone(ZONE)
                    .format(Instant.ofEpochSecond(sec));
        } catch (Exception e) {
            return "?";
        }
    }

    // Beregn næste N leveringsdatoer givet nextEpoch + interval + intervalCount
    private static List<ZonedDateTime> computeUpcomingDeliveries(String nextEpochSec,
                                                                 String interval,
                                                                 int intervalCount,
                                                                 int count) {
        List<ZonedDateTime> out = new ArrayList<>();
        if (nextEpochSec == null || nextEpochSec.isBlank()
                || interval == null || interval.isBlank()
                || intervalCount <= 0) {
            return out;
        }
        long sec = Long.parseLong(nextEpochSec);
        ZonedDateTime start = Instant.ofEpochSecond(sec).atZone(ZONE);

        String iv = interval.trim().toUpperCase(Locale.ROOT);

        ZonedDateTime d = start;
        for (int i = 0; i < count; i++) {
            out.add(d);
            switch (iv) {
                case "DAY":
                case "DAYS":
                    d = d.plusDays(intervalCount);
                    break;
                case "WEEK":
                case "WEEKS":
                    d = d.plusWeeks(intervalCount);
                    break;
                case "MONTH":
                case "MONTHS":
                    d = d.plusMonths(intervalCount);
                    break;
                default:
                    i = count; // afslut loop
            }
        }
        return out;
    }

    // Shipping via DTO
    private static void printShipping(LoopShippingAddressDTO shipping) {
        if (shipping == null) return;

        System.out.println("    [Shipping Address]");
        System.out.println("      name    : " +
                (shipping.getFirstName() != null ? shipping.getFirstName() : "") + " " +
                (shipping.getLastName() != null ? shipping.getLastName() : ""));
        System.out.println("      phone   : " + (shipping.getPhone() != null ? shipping.getPhone() : ""));
        System.out.println("      address1: " + (shipping.getAddress1() != null ? shipping.getAddress1() : ""));
        System.out.println("      address2: " + (shipping.getAddress2() != null ? shipping.getAddress2() : ""));
        System.out.println("      zip/city: " +
                (shipping.getZip() != null ? shipping.getZip() : "") + " " +
                (shipping.getCity() != null ? shipping.getCity() : ""));
        System.out.println("      country : " + (shipping.getCountryCode() != null ? shipping.getCountryCode() : ""));
    }

    private static void printPolicies(LoopBillingPolicyDTO billingPolicy,
                                      LoopDeliveryPolicyDTO deliveryPolicy) {
        System.out.println("    [Policies]");
        if (billingPolicy != null) {
            System.out.println("      Billing : interval=" +
                    billingPolicy.getInterval() +
                    ", intervalCount=" + billingPolicy.getIntervalCount());
        }
        if (deliveryPolicy != null) {
            System.out.println("      Delivery: interval=" +
                    deliveryPolicy.getInterval() +
                    ", intervalCount=" + deliveryPolicy.getIntervalCount());
        }
    }

    // ========== EKSISTERENDE HTTP-HELPERS ==========

    private HttpRequest buildGet(String fullUrl) throws URISyntaxException {
        return HttpRequest.newBuilder()
                .uri(new URI(fullUrl))
                .header("accept", "application/json")
                .header("X-Loop-Token", TOKEN)
                .GET()
                .build();
    }

    /** Sender GET med retry/backoff på 429 og 5xx. */
    private HttpResponse<String> sendWithRetry(HttpRequest req) throws IOException, InterruptedException {
        int maxRetries = 6;
        long backoffMs = 400;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            int code = res.statusCode();
            if (code < 400) return res;

            if (code == 429 || (code >= 500 && code < 600)) {
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

    // GAMMEL parse til JsonNode (brugt af getSubscriptionDetails)
    private JsonNode parseIf200(HttpResponse<String> res) throws IOException {
        if (res != null && res.statusCode() == 200) return mapper.readTree(res.body());
        return null;
    }

    // NY generisk parse til DTO'er
    private <T> T parseIf200Dto(HttpResponse<String> res, Class<T> clazz) throws IOException {
        if (res != null && res.statusCode() == 200) {
            return mapper.readValue(res.body(), clazz);
        }
        return null;
    }

    /** LIST: subscriptions globalt (valgfrit filter på status) + pagination – nu med DTO */
    public LoopSubscriptionsResponseDTO getSubscriptions(String status, int limit, int offset)
            throws IOException, InterruptedException, URISyntaxException {
        StringBuilder sb = new StringBuilder(BASE_URL).append("/subscription?limit=")
                .append(limit).append("&offset=").append(offset);
        if (status != null && !status.isBlank()) {
            sb.append("&status=").append(URLEncoder.encode(status, StandardCharsets.UTF_8));
        }
        HttpResponse<String> res = sendWithRetry(buildGet(sb.toString()));
        return parseIf200Dto(res, LoopSubscriptionsResponseDTO.class);
    }

    /** DETAILS: subscription (indeholder delivery/billing/lines) – stadig JsonNode-baseret for nu */
    public JsonNode getSubscriptionDetails(String subscriptionId)
            throws IOException, InterruptedException, URISyntaxException {
        String url = BASE_URL + "/subscription/" + URLEncoder.encode(subscriptionId, StandardCharsets.UTF_8);
        HttpResponse<String> res = sendWithRetry(buildGet(url));
        return parseIf200(res);
    }
}
