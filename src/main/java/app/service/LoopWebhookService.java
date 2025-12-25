package app.service;

import app.daos.CustomerDAO;
import app.dtos.LoopCustomerDTO;
import app.dtos.LoopSubscriptionDTO;
import app.dtos.LoopWebhookDTO;

public class LoopWebhookService
{

    private final LoopApiService loopApiService;
    private final LoopSyncService loopSyncService;


    public LoopWebhookService(LoopApiService loopApiService, LoopSyncService loopSyncService)
    {
        this.loopApiService = loopApiService;
        this.loopSyncService = loopSyncService;

    }

    public void handle(LoopWebhookDTO dto)
    {

        if (dto == null || dto.getEvent() == null || dto.getData() == null || dto.getData().getId() == null)
        {
            return;
        }

        try
        {
            switch (dto.getEvent())
            {

                case "subscription.updated" ->
                {

                    Long loopSubId = dto.getData().getId();
                    if (loopSubId == null) return;

                    // 1) hent subscription
                    LoopSubscriptionDTO sub = loopApiService.fetchSubscriptionById(loopSubId);
                    if (sub == null || sub.getCustomer() == null) return;

                    // ✅ TEST-OVERRIDE (kun til lokal test)
                    if (dto.getData().getStatus() != null && !dto.getData().getStatus().isBlank()) {
                        sub.setStatus(dto.getData().getStatus());
                    }

                    // 2) hent shopifyId fra subscription
                    Long shopifyCustomerId = sub.getCustomer().getShopifyId();
                    if (shopifyCustomerId == null) return;

                    // 3) hent customer fra Loop via SHOPIFY id
                    LoopCustomerDTO customer = loopApiService.fetchCustomerByShopifyId(shopifyCustomerId);

                    // 4) sync customer lokalt
                    if (customer != null)
                    {
                        loopSyncService.syncCustomer(customer);
                    }

                    // 5) sync subscription (sætter relationen)
                    loopSyncService.syncSubscription(sub);

                    System.out.println("✅ Synced subscription to DB: " + sub.getLoopSubscriptionId());
                }


                default -> System.out.println("⚠️ Ignorerer event: " + dto.getEvent());
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
