package org.example.backend_pcbuild.Admin.service;

import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.models.Component;
import org.example.backend_pcbuild.models.GpuModel;
import org.example.backend_pcbuild.models.Offer;
import org.example.backend_pcbuild.repository.OfferRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OfferAdminService {

    public  Optional<Offer> findBestForCpu(OfferRepository repo, Component comp, double budget) {
        if (comp == null) return Optional.empty();
        if (budget == 0) {
            List<Offer> list = repo.findByComponentOrderByPriceAsc(comp);
            return  list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } else {
            List<Offer> list = repo.findByComponentOrderByBudgetPriceAsc(comp.getId(), budget);
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        }
    }

    public  Optional<Offer> findBestForGpuModel(OfferRepository repo, GpuModel gm, double budget) {
        if (gm == null) return Optional.empty();
        if (budget == 0) {
            List<Offer> byGpuModelOrderByPriceAsc = repo.findByGpuModelOrderByPriceAsc(gm);
            return  byGpuModelOrderByPriceAsc.stream().findFirst();
        } else {
            List<Offer> list = repo.findByGpuModelAndPriceLessThanEqualOrderByPriceAsc(gm, budget, PageRequest.of(0,1));
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        }
    }
}
