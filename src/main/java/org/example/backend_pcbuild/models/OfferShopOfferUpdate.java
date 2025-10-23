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

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "offer_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Offer offer;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "shop_offer_update_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ShopOfferUpdate shopOfferUpdate;
}