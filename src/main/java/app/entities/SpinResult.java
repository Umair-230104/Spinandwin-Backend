package app.entities;

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

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @ManyToOne
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @ManyToOne
    @JoinColumn(name = "segment_id")
    private WheelSegment segment;

    private LocalDateTime spunAt;

    private boolean prizeApplied;       // om præmien er lagt på næste levering
    private String appliedToOrderId;    // fx Shopify/Loop order id
}
