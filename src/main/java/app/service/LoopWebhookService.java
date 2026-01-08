package app.service;

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
                    LoopSubscriptionDTO sub = loopApiService.fetchSubscriptionById(loopSubId);
                    if (sub == null || sub.getCustomer() == null) return;
                    if (dto.getData().getStatus() != null && !dto.getData().getStatus().isBlank())
                    {
                        sub.setStatus(dto.getData().getStatus());
                    }
                    // sync subscription (med customerShopifyId som reference)
                    loopSyncService.syncSubscription(sub);
                    System.out.println("✅ Synced subscription to DB: " + sub.getLoopSubscriptionId());
                }

                case "customer.updated" ->
                {
                    Long shopifyCustomerId = dto.getData().getId();
                    if (shopifyCustomerId == null) return;
                    LoopCustomerDTO customer = loopApiService.fetchCustomerByShopifyId(shopifyCustomerId);
                    if (customer == null) return;
                    loopSyncService.syncCustomer(customer);
                    System.out.println("✅ Synced customer to DB: " + customer.getShopifyId());
                }
                default -> System.out.println("⚠️ Ignorerer event: " + dto.getEvent());
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
