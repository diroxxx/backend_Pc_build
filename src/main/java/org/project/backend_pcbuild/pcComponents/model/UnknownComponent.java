package org.project.backend_pcbuild.pcComponents.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
@DiscriminatorValue("UNKNOWN")
public class UnknownComponent extends Component {
}
