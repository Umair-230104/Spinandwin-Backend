package app.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityResponseDTO {

    private boolean eligible;     // true = må spinne, false = må ikke
    private String reasonCode;    // fx "OK", "NOT_FOUND", "NO_ACTIVE_SUB", "NO_DELIVERED_DELIVERY"

    // Lidt ekstra info til frontend/UI (valgfrit)
    private String customerName;
    private String email;
    private String phone;
}
