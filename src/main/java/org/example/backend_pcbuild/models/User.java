package org.example.backend_pcbuild.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    @Size(min = 2, max = 100)
    private String username;

    @NotNull
    @Column(unique = true)
    @Size(min = 2, max = 100)
    private String email;
    @NotNull
    @Size(min = 2, max = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull
    private UserRole role;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = UserRole.USER;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public @NotNull UserRole getRole() {
        return role;
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Computer> computers;
}
