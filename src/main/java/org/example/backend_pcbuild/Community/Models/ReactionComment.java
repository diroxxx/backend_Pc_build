package org.example.backend_pcbuild.Community.Models;


import jakarta.persistence.*;
import lombok.Data;
import org.example.backend_pcbuild.models.User;

@Entity
@Data
@Table(name = "comment_reactions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "comment_id"})
        })
public class ReactionComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean likeReaction = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false)
    private PostComment comment;
}
