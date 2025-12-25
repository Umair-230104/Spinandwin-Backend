package app.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoopWebhookCustomerDataDTO {
    private Long shopifyId;
    private Long loopCustomerId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Integer activeSubscriptionsCount;
}
