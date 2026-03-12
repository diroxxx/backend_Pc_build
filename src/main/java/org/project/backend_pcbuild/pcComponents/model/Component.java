package org.project.backend_pcbuild.pcComponents.model;

import jakarta.persistence.*;
import lombok.Data;
import org.project.backend_pcbuild.offer.model.Brand;
import org.project.backend_pcbuild.offer.model.Offer;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "component_type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "component")
public abstract class Component {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", insertable = false, updatable = false)
    private ComponentType componentType;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL)
    private Set<Offer> offers = new HashSet<>();

}