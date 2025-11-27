package app.dtos;

import app.Enums.SegmentType;
import app.entities.WheelSegment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

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

    public WheelSegmentDTO(WheelSegment wheelSegment) {
        this.id = wheelSegment.getId();
        this.wheelId = wheelSegment.getWheel().getId();
        this.position = wheelSegment.getPosition();
        this.type = wheelSegment.getType();
        this.title = wheelSegment.getTitle();
        this.imageUrl = wheelSegment.getImageUrl();
        this.prizeName = wheelSegment.getPrizeName();
        this.discountCode = wheelSegment.getDiscountCode();
        this.productSku = wheelSegment.getProductSku();
        this.active = wheelSegment.isActive();
    }

    public static List<WheelSegmentDTO> toWheelSegmentList(List<WheelSegment> wheelSegments) {
        return wheelSegments.stream().map(WheelSegmentDTO::new).collect(Collectors.toList());
    }
}