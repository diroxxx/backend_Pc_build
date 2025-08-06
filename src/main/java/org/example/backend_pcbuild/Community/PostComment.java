package org.example.backend_pcbuild.Community;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.backend_pcbuild.models.User;

import java.time.LocalDateTime;

@Entity
@Table
@Setter
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String content;

    @Getter
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Getter
    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @NotNull
    private LocalDateTime createdAt;

//    public void setPost(Post post) {
//        this.post = post;
//    }
//
//    public void setUser(User user) {
//        this.user = user;
//    }
}
