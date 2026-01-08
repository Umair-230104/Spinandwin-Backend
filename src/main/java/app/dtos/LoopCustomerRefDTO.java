package app.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoopCustomerRefDTO
{
    private Long id;          // Loop customer id
    private Long shopifyId;   // kan bruges senere til at hente shopify customer id direkte fra Loop
}
