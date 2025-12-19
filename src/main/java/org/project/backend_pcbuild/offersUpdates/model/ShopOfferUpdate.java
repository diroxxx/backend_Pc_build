package org.project.backend_pcbuild.offersUpdates.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.project.backend_pcbuild.offer.model.Shop;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(
        name = "shop_offer_update",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"offer_update_id", "shop_id"})
        }
)
public class ShopOfferUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShopUpdateStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Shop shop;

    @ManyToOne(optional = false,cascade = CascadeType.PERSIST)
    @JoinColumn(name = "offer_update_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OfferUpdate offerUpdate;

    @OneToMany(mappedBy = "shopOfferUpdate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OfferShopOfferUpdate> offerShopOfferUpdates = new ArrayList<>();


}
