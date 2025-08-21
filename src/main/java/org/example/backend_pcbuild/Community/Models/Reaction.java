package org.example.backend_pcbuild.Community.Models;


import jakarta.persistence.*;
import lombok.Data;
import org.example.backend_pcbuild.Community.Models.Post;
import org.example.backend_pcbuild.models.User;

@Entity
@Data
public class Reaction {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private Boolean likeReaction = false;

    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;


}
