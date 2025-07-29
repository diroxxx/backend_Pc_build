package org.example.backend_pcbuild.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @NotNull
//    @Size(min = 1, max = 100)
    private String brand;

//    @NotNull
//    @Size(min = 1, max = 100)
    private String model;


    @Enumerated(EnumType.STRING)
    @Column(name = "item_condition")
    private ItemCondition condition;

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

    @OneToMany(mappedBy = "item")
    private Set<Computer_Item> computer_item = new HashSet<Computer_Item>();

    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private GraphicsCard graphicsCard;

    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private Processor processor;

    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private Case case_;

    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cooler cooler;

    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private Memory memory;

    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private Motherboard motherboard;

    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private PowerSupply powerSupply;

    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private Storage storage;





}
