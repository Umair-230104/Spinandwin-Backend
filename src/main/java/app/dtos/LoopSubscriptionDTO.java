package app.dtos;

import app.entities.Subscription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoopSubscriptionDTO
{

    @JsonProperty("id")
    private Long loopSubscriptionId;                         // Loop subscription id
    private String status;                   // ACTIVE / PAUSED / CANCELLED
    private Long nextBillingDateEpoch;       // bruges til næste levering
    private String lastPaymentStatus;        // SUCCESS / FAILED etc.
    private LoopCustomerRefDTO customer;
    private LoopShippingAddressDTO shippingAddress;
    private LoopBillingPolicyDTO billingPolicy;
    private LoopDeliveryPolicyDTO deliveryPolicy;

    public LoopSubscriptionDTO(Subscription subscription)
    {
        this.loopSubscriptionId = subscription.getLoopSubscriptionId();
        this.status = subscription.getStatus().name();
        if (subscription.getNextBillingAt() != null)
        {
            this.nextBillingDateEpoch = subscription.getNextBillingAt().toEpochSecond(java.time.ZoneOffset.UTC);
        }
    }
}
