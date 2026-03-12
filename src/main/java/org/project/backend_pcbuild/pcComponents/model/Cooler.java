package org.project.backend_pcbuild.pcComponents.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("CPU_COOLER")
@Table(name = "cooler")
public class Cooler extends Component {

    @ElementCollection
    @CollectionTable(name = "cooler_socket_types", joinColumns = @JoinColumn(name = "cooler_id"))
    @Column(name = "socket_type")
    private List<String> socketTypes;

    private String fanRpm;
    private String noiseLevel;
    private String radiatorSize;

}