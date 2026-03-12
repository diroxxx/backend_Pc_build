package org.project.backend_pcbuild.pcComponents.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("MOTHERBOARD")
@Table(name = "motherboard")
public class Motherboard extends Component {

    private String chipset;
    private String socketType;
    private String format;
    private Integer ramSlots;
    private Integer ramCapacity;
    private String memoryType;

}