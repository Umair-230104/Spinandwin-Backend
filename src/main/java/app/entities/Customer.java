package app.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;           // kan være null hvis de kun bruger telefon
    private String phone;           // kan være null hvis de kun bruger email

    private Long loopCustomerId;    // id fra Loop API

    @Column(nullable = false)
    private boolean activeSubscription; // true/false – kan holdes i sync med Loop

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
