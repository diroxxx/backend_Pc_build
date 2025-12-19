package org.project.backend_pcbuild.community.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.project.backend_pcbuild.usersManagement.model.User;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table
@Data
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false)
    private String content;


    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;


    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @NotNull
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReactionComment> reactions;



}
