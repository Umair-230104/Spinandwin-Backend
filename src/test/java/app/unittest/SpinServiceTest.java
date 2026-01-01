package app.unittest;

import app.Enums.SegmentType;
import app.daos.DeliveryDAO;
import app.daos.SpinResultDAO;
import app.daos.WheelSegmentDAO;
import app.dtos.SpinResponseDTO;
import app.entities.Delivery;
import app.entities.SpinResult;
import app.entities.Subscription;
import app.entities.WheelSegment;
import app.service.SpinService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpinServiceTest {

    private DeliveryDAO deliveryDAO;
    private SpinResultDAO spinResultDAO;
    private WheelSegmentDAO wheelSegmentDAO;

    private SpinService service;

    @BeforeEach
    void setup() {
        deliveryDAO = mock(DeliveryDAO.class);
        spinResultDAO = mock(SpinResultDAO.class);
        wheelSegmentDAO = mock(WheelSegmentDAO.class);

        service = new SpinService(
                deliveryDAO,
                spinResultDAO,
                wheelSegmentDAO
        );
    }

    @Test
    void spin_noDeliveredDelivery_throwsException() {

        when(deliveryDAO.findLatestDeliveredByCustomer(1L))
                .thenReturn(null);

        Exception ex = assertThrows(Exception.class, () ->
                service.spin(1L)
        );

        assertTrue(ex.getMessage().contains("spin not allowed"));
    }

    @Test
    void spin_alreadySpun_throwsException() {

        Delivery delivery = new Delivery();
        delivery.setId(10L);

        Subscription sub = new Subscription();
        sub.setId(5L);
        delivery.setSubscription(sub);

        when(deliveryDAO.findLatestDeliveredByCustomer(1L))
                .thenReturn(delivery);

        when(spinResultDAO.findByCustomerAndDelivery(1L, 10L))
                .thenReturn(new SpinResult());

        Exception ex = assertThrows(Exception.class, () ->
                service.spin(1L)
        );

        assertTrue(ex.getMessage().contains("already spun"));
    }

    @Test
    void spin_validSpin_returnsResult() throws Exception {

        Delivery delivery = new Delivery();
        delivery.setId(10L);

        Subscription sub = new Subscription();
        sub.setId(5L);
        delivery.setSubscription(sub);

        WheelSegment segment = new WheelSegment();
        segment.setActive(true);
        segment.setType(SegmentType.TRY_AGAIN);
        segment.setTitle("Prøv igen");

        when(deliveryDAO.findLatestDeliveredByCustomer(1L))
                .thenReturn(delivery);

        when(spinResultDAO.findByCustomerAndDelivery(1L, 10L))
                .thenReturn(null);

        when(wheelSegmentDAO.getAll())
                .thenReturn(List.of(segment));

        SpinResponseDTO result = service.spin(1L);

        assertEquals(SegmentType.TRY_AGAIN, result.getType());
        assertTrue(result.isCanSpinAgain());

        verify(spinResultDAO).create(any(SpinResult.class));
    }
}
