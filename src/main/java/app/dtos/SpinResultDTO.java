package app.dtos;

import app.Enums.SegmentType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpinResultDTO {

    private Long spinResultId;

    private SegmentType type;

    private String title;
    private String imageUrl;

    // kun relevant hvis PRIZE
    private String prizeName;
    private String discountCode;
    private String productSku;
}
