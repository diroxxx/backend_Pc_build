package org.example.backend_pcbuild.Community.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.backend_pcbuild.Community.Models.Post;
import org.example.backend_pcbuild.models.User;

import java.time.LocalDateTime;

@Entity
@Table
@Data
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String content;


    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;


    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @NotNull
    private LocalDateTime createdAt;

}
