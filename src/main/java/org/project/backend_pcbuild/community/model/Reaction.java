package org.project.backend_pcbuild.community.model;


import jakarta.persistence.*;
import lombok.Data;
import org.project.backend_pcbuild.usersManagement.model.User;

@Entity
@Data
@Table(name = "post_likes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "post_id"})
        })
public class Reaction {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean likeReaction = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;


}
