package app.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoopCustomerRefDTO {
    private Long id;          // Loop customer id
    private Long shopifyId;   // kan bruges senere, optional
}
