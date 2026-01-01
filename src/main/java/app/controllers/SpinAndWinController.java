package app.controllers;

import app.daos.WheelSegmentDAO;
import app.dtos.*;
import app.entities.WheelSegment;
import app.exception.ApiException;
import app.service.CustomerEligibilityService;
import app.service.SpinService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SpinAndWinController {

    private final Logger log = LoggerFactory.getLogger(SpinAndWinController.class);

    private final WheelSegmentDAO wheelSegmentDAO;
    private final CustomerEligibilityService eligibilityService;
    private final SpinService spinService;


    public SpinAndWinController(WheelSegmentDAO wheelSegmentDAO,
                                CustomerEligibilityService eligibilityService, SpinService spinService) {
        this.wheelSegmentDAO = wheelSegmentDAO;
        this.eligibilityService = eligibilityService;
        this.spinService = spinService;
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

    // POST /spin
    public void spin(Context ctx) {
        try {
            SpinRequestDTO req = ctx.bodyAsClass(SpinRequestDTO.class);

            SpinResponseDTO response = spinService.spin(req.getCustomerId());

            ctx.status(200).json(response);

        } catch (Exception e) {
            log.error("400 {}", e.getMessage());
            throw new ApiException(400, e.getMessage());
        }
    }

    // GET /spin-result/{id}
    public void getSpinResult(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));

            SpinResultDTO result = spinService.getSpinResult(id);

            ctx.status(200).json(result);

        } catch (Exception e) {
            log.error("404 {}", e.getMessage());
            throw new ApiException(404, e.getMessage());
        }
    }

}
