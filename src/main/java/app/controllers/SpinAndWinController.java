package app.controllers;

import app.daos.WheelSegmentDAO;
import app.dtos.EligibilityRequestDTO;
import app.dtos.EligibilityResponseDTO;
import app.dtos.WheelSegmentDTO;
import app.entities.WheelSegment;
import app.exception.ApiException;
import app.service.CustomerEligibilityService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SpinAndWinController {

    private final Logger log = LoggerFactory.getLogger(SpinAndWinController.class);

    private final WheelSegmentDAO wheelSegmentDAO;
    private final CustomerEligibilityService eligibilityService;

    public SpinAndWinController(WheelSegmentDAO wheelSegmentDAO,
                                CustomerEligibilityService eligibilityService) {
        this.wheelSegmentDAO = wheelSegmentDAO;
        this.eligibilityService = eligibilityService;
    }

    public void getAllWheelSegments(Context ctx) {
        try {
            List<WheelSegment> wheelSegments = wheelSegmentDAO.getAll();
            List<WheelSegmentDTO> wheelSegmentDTOS = WheelSegmentDTO.toWheelSegmentList(wheelSegments);

            ctx.status(200);
            ctx.json(wheelSegmentDTOS);
        } catch (Exception e) {
            log.error("500 {} ", e.getMessage());
            throw new ApiException(500, e.getMessage());
        }
    }

    public void updateWheelSegment(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            WheelSegmentDTO wheelSegmentDTO = ctx.bodyAsClass(WheelSegmentDTO.class);

            WheelSegmentDTO updatedWheelSegmentDTO = wheelSegmentDAO.update(id, wheelSegmentDTO);

            ctx.status(200);
            ctx.json(updatedWheelSegmentDTO);
        } catch (Exception e) {
            log.error("400 {} ", e.getMessage());
            throw new ApiException(400, e.getMessage());
        }
    }

    // ✅ NY ENDPOINT: POST /api/customer/check-eligibility
    public void checkEligibility(Context ctx) {
        try {
            EligibilityRequestDTO req = ctx.bodyAsClass(EligibilityRequestDTO.class);

            EligibilityResponseDTO response =
                    eligibilityService.checkEligibility(req.getEmailOrPhone());

            ctx.status(200);
            ctx.json(response);
        } catch (Exception e) {
            log.error("400 {} ", e.getMessage());
            throw new ApiException(400, e.getMessage());
        }
    }
}
