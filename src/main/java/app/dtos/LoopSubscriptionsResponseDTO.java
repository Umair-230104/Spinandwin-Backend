package app.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoopSubscriptionsResponseDTO {
    private boolean success;
    private String message;
    private List<LoopSubscriptionDTO> data;
}
