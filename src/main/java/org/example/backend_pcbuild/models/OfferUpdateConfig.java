package org.example.backend_pcbuild.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.example.backend_pcbuild.Admin.dto.OfferUpdateType;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
public class OfferUpdateConfig {

    public OfferUpdateConfig( OfferUpdateType type, Integer intervalInMinutes) {
        this.type = type;
        this.intervalInMinutes = intervalInMinutes;
    }

    public OfferUpdateConfig() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    private OfferUpdateType type;

    //in minutes
//    @NotNull
    private Integer intervalInMinutes;

    @OneToMany(mappedBy = "offerUpdateConfig")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Set<OfferUpdate> offerUpdates = new HashSet<>();


}
