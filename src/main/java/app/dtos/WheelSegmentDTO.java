package app.dtos;

import app.Enums.SegmentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WheelSegmentDTO
{
    private Long id;
    private Long wheelId;
    private Integer position;
    private SegmentType type;
    private String title;
    private String imageUrl;
    private String prizeName;
    private String discountCode;
    private String productSku;
    private boolean active;
}