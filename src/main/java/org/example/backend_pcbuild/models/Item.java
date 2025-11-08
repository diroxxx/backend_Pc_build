package org.example.backend_pcbuild.models;

import jakarta.persistence.*;
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
//    @Column(nullable = false, unique = true)
    private String brand;

//    @NotNull
//    @Size(min = 1, max = 100)
    private String model;

    @Enumerated(EnumType.STRING)
    private ComponentType componentType;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private Set<Offer> offers = new HashSet<Offer>();


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
