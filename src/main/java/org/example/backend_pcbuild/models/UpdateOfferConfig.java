package org.example.backend_pcbuild.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "update_offer_config")
public class UpdateOfferConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    private UpdateOfferType type;

    //in minutes
    @NotNull
    private Integer interval;
}
