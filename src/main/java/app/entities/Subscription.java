package app.entities;

import app.Enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long loopSubscriptionId;   // Loop subscription id
    private Long shopifyId;            // Shopify subscription id

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status; // fx "ACTIVE", "PAUSED", "CANCELLED"

    private Integer completedOrdersCount;
    private Boolean prepaid;

    private LocalDateTime nextBillingAt;

    private String interval;           // fx "WEEK", "MONTH"
    private Integer intervalCount;     // fx 1, 2, 4 ...

    @CreationTimestamp
    private LocalDateTime lastSyncedAt;
}
