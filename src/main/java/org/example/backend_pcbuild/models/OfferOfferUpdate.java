package org.example.backend_pcbuild.models;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"offer_id", "offer_update_id"})
)
public class OfferOfferUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Offer offer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "offer_update_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OfferUpdate offerUpdate;
}