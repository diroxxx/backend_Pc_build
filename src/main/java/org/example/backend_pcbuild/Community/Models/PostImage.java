package org.example.backend_pcbuild.Community.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Lob
    private byte[] image;


    @Column(length = 50)
    private String mimeType;


    @Column(length = 255)
    private String filename;


    private int displayOrder;

    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
//    @JsonBackReference
    private Post post;

}