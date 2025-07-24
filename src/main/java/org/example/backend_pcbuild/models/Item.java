package org.example.backend_pcbuild.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1, max = 100)
    private String brand;

    @NotNull
    @Size(min = 1, max = 100)
    private String model;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "item_condition")
    private ItemCondition condition;

    @NotBlank
    private String photo_url;

    @NotNull
    private String website_url;

    @NotNull
    private double price;

    @NotNull
    private String shop;

    @OneToMany(mappedBy = "item")
    private Set<Computer_Item> computer_item = new HashSet<Computer_Item>();

    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private GraphicsCard graphicsCard;

}
