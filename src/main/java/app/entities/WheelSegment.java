package app.entities;

import app.Enums.SegmentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wheel_segments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WheelSegment
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "wheel_id")
    private SpinWheel wheel;
    private Integer position;      // 0-8 på hjulet

    @Enumerated(EnumType.STRING)
    private SegmentType type;      // "PRIZE", "TRY_AGAIN", "NO_WIN"
    private String title;          // tekst vist på hjulet
    private String imageUrl;       // billede til hjulet/vundet-side
    private String prizeName;      // kun bruges hvis type = "PRIZE"
    private String discountCode;   // rabatkode
    private String productSku;     // gratis produkt, hvis relevant
    private boolean active;        // kan slås fra i admin-panel
}
