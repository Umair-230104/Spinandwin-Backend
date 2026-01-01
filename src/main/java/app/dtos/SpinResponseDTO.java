package app.dtos;

import app.Enums.SegmentType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpinResponseDTO {

    private Long spinResultId;   // bruges til vundet-side (GET /spin-result/{id})

    private SegmentType type;    // PRIZE, TRY_AGAIN, NO_WIN

    private String title;        // tekst der vises på hjulet / resultat
    private String imageUrl;     // billede der vises

    private boolean canSpinAgain; // true hvis TRY_AGAIN
}
