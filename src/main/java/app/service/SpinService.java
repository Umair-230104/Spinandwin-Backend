package app.service;

import app.daos.DeliveryDAO;
import app.daos.SpinResultDAO;
import app.daos.WheelSegmentDAO;
import app.dtos.SpinResponseDTO;
import app.dtos.SpinResultDTO;
import app.entities.Delivery;
import app.entities.SpinResult;
import app.entities.WheelSegment;
import app.Enums.SegmentType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class SpinService {

    private final DeliveryDAO deliveryDAO;
    private final SpinResultDAO spinResultDAO;
    private final WheelSegmentDAO wheelSegmentDAO;

    public SpinService(
            DeliveryDAO deliveryDAO,
            SpinResultDAO spinResultDAO,
            WheelSegmentDAO wheelSegmentDAO
    ) {
        this.deliveryDAO = deliveryDAO;
        this.spinResultDAO = spinResultDAO;
        this.wheelSegmentDAO = wheelSegmentDAO;
    }

    public SpinResultDTO getSpinResult(Long spinResultId) throws Exception {

        SpinResult spinResult = spinResultDAO.getById(spinResultId);

        if (spinResult == null) {
            throw new Exception("Spin result not found");
        }

        WheelSegment segment = spinResult.getWheelSegment();

        return SpinResultDTO.builder()
                .spinResultId(spinResult.getId())
                .type(spinResult.getResultType())
                .title(segment.getTitle())
                .imageUrl(segment.getImageUrl())
                .prizeName(segment.getPrizeName())
                .discountCode(segment.getDiscountCode())
                .productSku(segment.getProductSku())
                .build();
    }


    public SpinResponseDTO spin(Long customerId) throws Exception {

        // 1️⃣ Find seneste leverede (SHIPPED) delivery
        Delivery delivery = deliveryDAO.findLatestDeliveredByCustomer(customerId);

        if (delivery == null) {
            throw new Exception("Customer has no shipped deliveries – spin not allowed");
        }

        // 2️⃣ Tjek om kunden allerede har spundet for denne delivery
        SpinResult existing =
                spinResultDAO.findByCustomerAndDelivery(customerId, delivery.getId());

        if (existing != null) {
            throw new Exception("Customer has already spun for this delivery");
        }

        // 3️⃣ Hent aktive wheel segments
        List<WheelSegment> activeSegments = wheelSegmentDAO.getAll()
                .stream()
                .filter(WheelSegment::isActive)
                .toList();

        if (activeSegments.isEmpty()) {
            throw new Exception("No active wheel segments available");
        }

        // 4️⃣ Vælg random segment
        WheelSegment selected =
                activeSegments.get(new Random().nextInt(activeSegments.size()));

        // 5️⃣ Opret SpinResult
        SpinResult spinResult = new SpinResult();
        spinResult.setCustomerId(customerId);
        spinResult.setSubscriptionId(delivery.getSubscription().getId());
        spinResult.setDeliveryId(delivery.getId());
        spinResult.setWheelSegment(selected);
        spinResult.setResultType(selected.getType()); // PRIZE / TRY_AGAIN / NO_WIN
        spinResult.setPrizeApplied(false);
        spinResult.setCreatedAt(LocalDateTime.now());

        spinResultDAO.create(spinResult);

        // 6️⃣ Returnér respons til frontend
        return SpinResponseDTO.builder()
                .spinResultId(spinResult.getId())
                .type(spinResult.getResultType())
                .title(selected.getTitle())
                .imageUrl(selected.getImageUrl())
                .canSpinAgain(selected.getType() == SegmentType.TRY_AGAIN)
                .build();
    }
}
