package app.entities;

import app.Enums.SegmentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@Table(name = "spin_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpinResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // hvem
    private Long customerId;

    // hvilken subscription / levering
    private Long subscriptionId;
    private Long deliveryId;

    // hvad ramte kunden
    @ManyToOne
    @JoinColumn(name = "wheel_segment_id")
    private WheelSegment wheelSegment;

    @Enumerated(EnumType.STRING)
    private SegmentType resultType; // PRIZE, TRY_AGAIN, NO_WIN

    private boolean prizeApplied;

    private LocalDateTime createdAt;
}
