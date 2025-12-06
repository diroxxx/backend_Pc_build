package org.example.backend_pcbuild.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

@Entity
@Data
@Table(name = "computer_offer")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

public class ComputerOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "computer_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private Computer computer;

    @ManyToOne
    @JoinColumn(name = "offer", nullable = false)
    @EqualsAndHashCode.Exclude
    private Offer offer;
}
