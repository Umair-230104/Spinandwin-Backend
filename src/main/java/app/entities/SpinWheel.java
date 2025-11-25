package app.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "spin_wheels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpinWheel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;       // fx "Standard hjul"

    private boolean active;    // hvilket hjul der bruges nu

    @OneToMany(mappedBy = "wheel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WheelSegment> segments; // typisk 9 felter
}
