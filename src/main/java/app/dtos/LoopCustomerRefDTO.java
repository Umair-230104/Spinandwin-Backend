package app.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoopCustomerRefDTO {
    private long id;          // Loop customer id
    private long shopifyId;   // kan bruges senere, optional
}
