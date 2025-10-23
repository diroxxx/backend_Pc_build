package org.example.backend_pcbuild.Admin.service;

import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Admin.dto.OfferUpdateType;
import org.example.backend_pcbuild.Admin.repository.OfferUpdateConfigRepository;
import org.example.backend_pcbuild.models.OfferUpdateConfig;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OfferUpdateConfigService {

    private final OfferUpdateConfigRepository offerUpdateConfigRepository;

    public void saveOfferUpdateConfig(Integer intervalInMinutes, OfferUpdateType type){
        Optional<OfferUpdateConfig> offerUpdateConfig = offerUpdateConfigRepository.findByType(type);
        if(offerUpdateConfig.isPresent()) {
            return;
        }
        offerUpdateConfigRepository.save(new OfferUpdateConfig( type, intervalInMinutes));
    }
}
