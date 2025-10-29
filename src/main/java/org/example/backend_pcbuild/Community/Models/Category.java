//package org.example.backend_pcbuild.Community.Models;
//
//import jakarta.persistence.*;
//import lombok.Data;
//
//import java.util.HashSet;
//import java.util.Set;
//
//
//@Entity
//@Table
//@Data
//public class Category {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(length = 100, nullable = false)
//    private String name;
//
//    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
//    private Set<Post> posts = new HashSet<>();
//
//}
package org.example.backend_pcbuild.Community.Models;

import com.fasterxml.jackson.annotation.JsonIgnore; // ⬅️ Dodaj ten import
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "category") // Dobra praktyka: jawne nazwanie tabeli
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Post> posts = new HashSet<>();

}