package app.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoopSubscriptionDTO {
    private long id;                         // Loop subscription id
    private String status;                   // ACTIVE / PAUSED / CANCELLED
    private long nextBillingDateEpoch;       // bruges til næste levering
    private String lastPaymentStatus;        // SUCCESS / FAILED etc.
    private LoopCustomerRefDTO customer;
    private LoopShippingAddressDTO shippingAddress;
    private LoopBillingPolicyDTO billingPolicy;
    private LoopDeliveryPolicyDTO deliveryPolicy;
}
