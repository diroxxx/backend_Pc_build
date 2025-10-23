package org.example.backend_pcbuild.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class ShopOfferUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
