package org.project.backend_pcbuild.pcComponents.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("MEMORY")
@Table(name = "memory")
public class Memory extends Component {

    private String type;
    private Integer capacity;
    private Integer speed;
    private Integer latency;
    private Integer amount;

}