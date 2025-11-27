package app.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoopSubscriptionsResponseDTO
{
    private boolean success;
    private String message;
    private List<LoopSubscriptionDTO> data;
    private String code;
    private LoopPageInfoDTO pageInfo;
}
