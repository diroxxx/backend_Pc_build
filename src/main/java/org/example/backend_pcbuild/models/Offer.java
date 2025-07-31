package org.example.backend_pcbuild.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    //    @NotBlank
    @Column(length = 1000) // Zwiększ z domyślnych 255 na 1000
    private String photo_url;

    //    @NotNull
    @Column(length = 1000) // Zwiększ z domyślnych 255 na 1000
    private String website_url;

    //    @NotNull
    private Double price;

    //    @NotNull
    private String shop;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_condition")
    private ItemCondition condition;

    @ManyToOne
    @JoinColumn(name = "item_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Item item;

    @OneToMany(mappedBy = "offer")
    private Set<Computer_Item> computer_offer = new HashSet<Computer_Item>();

}
