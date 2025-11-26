package app.entities;

import app.Enums.SubscriptionStatus;
import app.dtos.LoopSubscriptionDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long loopSubscriptionId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private LocalDateTime nextBillingAt;

    public Subscription(LoopSubscriptionDTO loopSubscriptionDTO)
    {
        this.loopSubscriptionId = loopSubscriptionDTO.getLoopSubscriptionId();
        this.status = SubscriptionStatus.valueOf(loopSubscriptionDTO.getStatus());
        this.nextBillingAt = LocalDateTime.ofEpochSecond(loopSubscriptionDTO.getNextBillingDateEpoch(), 0, java.time.ZoneOffset.UTC);
    }

    public Subscription(LoopSubscriptionDTO dto, Customer customer) {
        this.loopSubscriptionId = dto.getLoopSubscriptionId();
        this.customer = customer;

        if (dto.getStatus() != null) {
            this.status = SubscriptionStatus.valueOf(dto.getStatus());
        }

        if (dto.getNextBillingDateEpoch() != null) {
            this.nextBillingAt = LocalDateTime.ofEpochSecond(
                    dto.getNextBillingDateEpoch(), 0, java.time.ZoneOffset.UTC
            );
        }
    }

}
