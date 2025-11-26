package app.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoopCustomersResponseDTO {

    private boolean success;
    private String message;
    private List<LoopCustomerDTO> data;
    private String code;

    private LoopPageInfoDTO pageInfo; // 👈 NY
}
