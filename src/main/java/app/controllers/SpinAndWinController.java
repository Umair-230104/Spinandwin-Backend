package app.controllers;

import app.daos.WheelSegmentDAO;
import app.dtos.WheelSegmentDTO;
import app.entities.WheelSegment;
import app.exception.ApiException;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SpinAndWinController
{

    private final Logger log = LoggerFactory.getLogger(SpinAndWinController.class);
    WheelSegmentDAO wheelSegmentDAO;

    public SpinAndWinController(WheelSegmentDAO wheelSegmentDAO)
    {
        this.wheelSegmentDAO = wheelSegmentDAO;
    }

    public void getAllWheelSegments(Context ctx)
    {
        try
        {
            // == querying ==
            List<WheelSegment> wheelSegments = wheelSegmentDAO.getAll();

            // == response ==
            List<WheelSegmentDTO> wheelSegmentDTOS = WheelSegmentDTO.toWheelSegmentList(wheelSegments);

            ctx.res().setStatus(200);
            ctx.json(wheelSegmentDTOS, WheelSegmentDTO.class);
        } catch (Exception e)
        {
            log.error("500 {} ", e.getMessage());
            throw new ApiException(500, e.getMessage());
        }
    }

    public void updateWheelSegment(Context ctx)
    {
        try
        {
            // Extract the id from the path parameter
            int id = Integer.parseInt(ctx.pathParam("id"));

            // == request ==
            WheelSegmentDTO wheelSegmentDTO = ctx.bodyAsClass(WheelSegmentDTO.class);

            // == querying ==
            WheelSegmentDTO updatedWheelSegmentDTO = wheelSegmentDAO.update(id, wheelSegmentDTO);

            // == response ==
            ctx.res().setStatus(200);
            ctx.json(updatedWheelSegmentDTO, WheelSegmentDTO.class);
            ctx.result("Invitation updated");
        } catch (Exception e)
        {
            log.error("400 {} ", e.getMessage());
            throw new ApiException(400, e.getMessage());
        }
    }


}
