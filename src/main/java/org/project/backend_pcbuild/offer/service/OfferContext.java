package org.project.backend_pcbuild.offer.service;

import org.project.backend_pcbuild.offer.dto.ComponentOfferDto;

class OfferContext {
    final String brand;
    final String title;
    final String modelRaw;
    final String modelLower;
    
    OfferContext(ComponentOfferDto dto) {
        this.brand = dto.getBrand();
        this.title = dto.getTitle();
        this.modelRaw = dto.getModel() != null ? dto.getModel() : this.title;
        this.modelLower = this.modelRaw != null ? this.modelRaw.toLowerCase() : "";
    }
}
