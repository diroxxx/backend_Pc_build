package org.project.backend_pcbuild.pcComponents.dto;

import java.util.List;

public record ShopPriceHistory(String shop, List<MonthlyPrice> monthlyPrices) {
}
