package org.project.backend_pcbuild.community.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.project.backend_pcbuild.usersManagement.model.User;

@Entity
@Getter
@Setter
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
