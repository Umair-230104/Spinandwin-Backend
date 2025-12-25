package app.controllers;

import app.dtos.LoopWebhookDTO;
import app.service.LoopWebhookService;
import io.javalin.http.Context;

public class LoopWebhookController {

    private final LoopWebhookService webhookService;

    public LoopWebhookController(LoopWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    public void receive(Context ctx) {
        LoopWebhookDTO dto = ctx.bodyAsClass(LoopWebhookDTO.class);
        webhookService.handle(dto);
        ctx.status(200).result("ok");
    }
}
