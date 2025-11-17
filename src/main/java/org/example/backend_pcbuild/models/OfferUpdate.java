package org.example.backend_pcbuild.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.aspectj.lang.annotation.Before;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "offer_update_config_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OfferUpdateConfig offerUpdateConfig;

    @OneToMany(mappedBy = "offerUpdate", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Set<ShopOfferUpdate> shopOfferUpdates = new HashSet<>();

}
