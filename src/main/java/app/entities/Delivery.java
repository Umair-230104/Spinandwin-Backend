package app.entities;

import app.Enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delivery
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String loopDeliveryId;     // hvis du får et id fra Loop

    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;      // fx "PENDING", "SHIPPED", "CANCELLED"
    private LocalDateTime shippedAt;   // sættes når levering er sendt
    private Integer sequenceNo;        // hvilken levering i rækken (1,2,3...)
}
