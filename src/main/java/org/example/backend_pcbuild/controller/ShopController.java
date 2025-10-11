package org.example.backend_pcbuild.controller;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.backend_pcbuild.models.Shop;
import org.example.backend_pcbuild.repository.ShopRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {
    private final ShopRepository shopRepository;

    @GetMapping()
    public ResponseEntity<List<ShopDto>> getShopName() {
        List<ShopDto> shopDtos = shopRepository.findAll().stream()
                .map(shop -> new ShopDto(shop.getName()))
                .toList();
        return ResponseEntity.ok(shopDtos);
    }

    @Setter
    @Getter
  static class ShopDto {
        private String name;
        public ShopDto(String name) { this.name = name; }
    }
}


