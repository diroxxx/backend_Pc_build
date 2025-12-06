package org.example.backend_pcbuild.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
public class Computer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 2,nullable = false)
    private Double price;

    @Length(min = 2, max = 50)
//    @Column(unique = true, nullable = false)
    private String name;

    private Boolean is_visible;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private User user;

    @OneToMany(mappedBy = "computer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComputerOffer> computer_offer = new ArrayList<>();

}
