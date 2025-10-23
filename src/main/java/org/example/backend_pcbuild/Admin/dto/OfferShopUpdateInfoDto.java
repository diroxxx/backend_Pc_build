package org.example.backend_pcbuild.Admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfferShopUpdateInfoDto {
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime finishedAt;
    private List<ShopUpdateInfoDto> shops;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShopUpdateInfoDto {
        String shopName;
        Map<String, Integer> totalOffers;
        Map<String, Integer> offersAdded;
        Map<String, Integer> offersDeleted;
    }
}