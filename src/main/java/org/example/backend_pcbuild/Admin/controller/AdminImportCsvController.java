package org.example.backend_pcbuild.Admin.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Admin.service.ImportCsvFilesService;
import org.example.backend_pcbuild.models.ComponentType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/admin/import")
@RequiredArgsConstructor
public class AdminImportCsvController {

    private final ImportCsvFilesService importCsvFilesService;

    @PostMapping(value = "/components", consumes = "multipart/form-data")
    public ResponseEntity<Integer> importCsv(@RequestPart("file") MultipartFile file, @RequestParam("componentType") ComponentType componentType) {
        try{
            Integer imported = importCsvFilesService.importComponentsFromCsv(file,componentType);
            return ResponseEntity.ok(imported);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
