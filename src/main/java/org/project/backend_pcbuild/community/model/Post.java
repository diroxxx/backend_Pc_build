package org.project.backend_pcbuild.community.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.project.backend_pcbuild.usersManagement.model.User;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 80, nullable = false)
    private String title;

    @Column(length = 2000, nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id")
//    @JsonIgnoreProperties({"id", "email", "password", "role", "posts", "comments", "computers"})
    @JsonIgnoreProperties("posts")
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("posts")
    private Category category;


    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private Set<PostComment> comments;

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Reaction> reactions;

    @NotNull
    private LocalDateTime createdAt;

    @EqualsAndHashCode.Exclude
    @JsonIgnore
//    @JsonManagedReference
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostImage> images;


    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SavedPost> savedByUsers;

}
