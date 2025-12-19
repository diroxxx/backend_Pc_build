package org.project.backend_pcbuild.offersUpdates.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class OfferUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //maybe add some validation startedAt < finishedAt
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OfferUpdateStatus status;

    @ManyToOne
    @JoinColumn(name = "offer_update_config_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OfferUpdateConfig offerUpdateConfig;

    @OneToMany(mappedBy = "offerUpdate", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<ShopOfferUpdate> shopOfferUpdates = new ArrayList<>();

}
