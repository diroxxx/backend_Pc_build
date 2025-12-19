package org.project.backend_pcbuild.computer.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.project.backend_pcbuild.offer.model.Offer;

@Entity
@Data
@Table(name = "computer_offer")
public class ComputerOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "computer_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Computer computer;

    @ManyToOne
    @JoinColumn(name = "offer", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Offer offer;
}
