package app.entities;

import app.dtos.LoopCustomerDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;

    @Column(unique = true)          // kan være null hvis de kun bruger telefon
    private String phone;           // kan være null hvis de kun bruger email
    private Long loopCustomerId;    // id fra Loop API

    @Column(unique = true)         // ✅ vigtig
    private Long shopifyId;        // Shopify customer id (bruges til /customer/{customerShopifyId})

    @Column(nullable = false)
    private boolean activeSubscription; // true/false – kan holdes i sync med Loop

    public Customer(LoopCustomerDTO loopCustomerDTO)
    {

        this.loopCustomerId = loopCustomerDTO.getLoopCustomerId();
        this.shopifyId = loopCustomerDTO.getShopifyId();
        this.firstName = loopCustomerDTO.getFirstName();
        this.lastName = loopCustomerDTO.getLastName();
        this.email = loopCustomerDTO.getEmail();
        this.phone = loopCustomerDTO.getPhone();
        this.activeSubscription =
                loopCustomerDTO.getActiveSubscriptionsCount() != null
                        && loopCustomerDTO.getActiveSubscriptionsCount() > 0;

    }
}
