package org.example.backend_pcbuild.models;

import com.opencsv.bean.CsvBindByName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Blob;
import java.sql.Clob;
import java.util.Base64;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CsvBindByName(column = "title")
    private String title;

    @Transient
    @CsvBindByName(column = "image_base64")
    @Lob
    private String imageBase64;

    @Lob
    private byte[] image;

    public void decodeImageFromBase64() {
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            this.image = Base64.getDecoder().decode(imageBase64);
        }
    }



}
