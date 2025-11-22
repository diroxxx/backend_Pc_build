package org.example.backend_pcbuild.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.backend_pcbuild.Admin.dto.OfferUpdateType;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
public class OfferUpdateConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    private OfferUpdateType type;

    //in minutes
    private String intervalTime;


    @OneToMany(mappedBy = "offerUpdateConfig" , cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Set<OfferUpdate> offerUpdates = new HashSet<>();


}
