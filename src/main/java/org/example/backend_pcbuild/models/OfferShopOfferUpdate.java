package org.example.backend_pcbuild.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
public class OfferShopOfferUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private UpdateChangeType updateChangeType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Offer offer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_offer_update_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ShopOfferUpdate shopOfferUpdate;


}