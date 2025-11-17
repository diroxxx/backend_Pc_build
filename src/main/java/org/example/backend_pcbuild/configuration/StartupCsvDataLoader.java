package org.example.backend_pcbuild.configuration;


import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Admin.service.ImportCsvFilesService;
import org.example.backend_pcbuild.models.ComponentType;
import org.example.backend_pcbuild.repository.ComponentRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class StartupCsvDataLoader implements ApplicationRunner {

    private final ComponentRepository componentRepository;
    private final ImportCsvFilesService importCsvFilesService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (componentRepository.count() > 0) {
            return;
        }
        loadCsv("csv/processors.csv", ComponentType.PROCESSOR);
        loadCsv("csv/cpu_coolers.csv", ComponentType.CPU_COOLER);
        loadCsv("csv/graphics_cards.csv", ComponentType.GRAPHICS_CARD);
        loadCsv("csv/memory.csv", ComponentType.MEMORY);
        loadCsv("csv/motherboards.csv", ComponentType.MOTHERBOARD);
        loadCsv("csv/storage.csv", ComponentType.STORAGE);
        loadCsv("csv/power_supplies.csv", ComponentType.POWER_SUPPLY);

    }

    private void loadCsv(String classpathLocation, ComponentType type) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        if (!resource.exists()) {
            System.out.println("Brak pliku CSV: " + classpathLocation);
            return;
        }
        try (InputStream is = resource.getInputStream()) {
            Integer imported = importCsvFilesService.importComponentsFromCsv(is, type);
            System.out.println("Zaimportowano " + imported + " rekordów z " + classpathLocation);
        }
    }
}
