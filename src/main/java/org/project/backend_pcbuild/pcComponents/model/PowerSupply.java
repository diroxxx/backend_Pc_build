package org.project.backend_pcbuild.pcComponents.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("POWER_SUPPLY")
@Table(name = "power_supply")
public class PowerSupply extends Component {

    private String modular;
    private String type;
    private String efficiencyRating;
    private Integer maxPowerWatt;

}