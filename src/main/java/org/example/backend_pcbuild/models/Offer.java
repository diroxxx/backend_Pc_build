package org.example.backend_pcbuild.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(
        name = "offer",
        uniqueConstraints = @UniqueConstraint(columnNames = {"shop_id", "website_url"})
)

public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotNull
    private String title;

    @NotNull
    @Column(length = 1000)
    private String photoUrl;

    @NotNull
    @Column(length = 1000)
    private String websiteUrl;

    @NotNull
    private Double price;


    @Enumerated(EnumType.STRING)
    @Column(name = "item_condition")
    private ItemCondition condition;

    private Boolean isVisible;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
//    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Shop shop;

    @ManyToOne
    @JoinColumn(name = "item_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Item item;

    @OneToMany(mappedBy = "offer",cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonIgnore
    private Set<ComputerOffer> computerOffers = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OfferShopOfferUpdate> offerShopOfferUpdates = new ArrayList<>();

}
