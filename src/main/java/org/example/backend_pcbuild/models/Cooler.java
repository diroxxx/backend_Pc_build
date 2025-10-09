package org.example.backend_pcbuild.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Cooler {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "cooler_socket_types", joinColumns = @JoinColumn(name = "cooler_id"))
    @Column(name = "socket_type")
    private List<String> socketTypes;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "item_id")
    private Item item;



}
