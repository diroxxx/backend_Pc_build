package org.example.backend_pcbuild.UserProfile;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.backend_pcbuild.Community.DTO.CategoryDTO;
import org.example.backend_pcbuild.Community.Models.Post;
import org.example.backend_pcbuild.Community.Repository.PostRepository;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.LoginAndRegister.dto.PostResponseDto;
import org.example.backend_pcbuild.LoginAndRegister.dto.UserDto;
import org.example.backend_pcbuild.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
//@CrossOrigin("http://127.0.0.1:5000")
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("/community")
public class UserController {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/posts/mine")
    public ResponseEntity<List<UserPostsDTO>> getMyPosts() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged in");
        }

        UserDto principalUserDto = (UserDto) authentication.getPrincipal();

        User user = userRepository.findByEmail(principalUserDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found."));

        List<Post> myPosts = postRepository.findByUserId(user.getId());

        // Mapowanie Post -> UserPostsDTO
        List<UserPostsDTO> dtoList = myPosts.stream().map(post -> {
            UserPostsDTO dto = new UserPostsDTO(
                    post.getId(),
                    post.getUser().getId(),
                    post.getTitle(),
                    post.getContent(),
                    new CategoryDTO(post.getCategory().getId(), post.getCategory().getName())
            );
            dto.setId(post.getId());
            dto.setUserId(post.getUser().getId());
            dto.setTitle(post.getTitle());
            dto.setContent(post.getContent());
            dto.setCategory(new CategoryDTO(post.getCategory().getId(), post.getCategory().getName()));
            return dto;
        }).toList();


        return ResponseEntity.ok(dtoList);
    }

//    @GetMapping("/posts/{userId}")
//    public ResponseEntity<List<UserPostsDTO>> getPostsByUser(@PathVariable Long userId) {
//
//        // Pobranie użytkownika po ID
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId));
//
//        // Pobranie postów użytkownika
//        List<Post> userPosts = postRepository.findByUserId(userId);
//
//        // Mapowanie Post -> UserPostsDTO z użyciem buildera
//        List<UserPostsDTO> dtoList = userPosts.stream()
//                .map(post -> UserPostsDTO.builder()
//                        .id(post.getId())
//                        .userId(user.getId())
//                        .title(post.getTitle())
//                        .content(post.getContent())
//                        .category(CategoryDTO.builder()
//                                .id(post.getCategory().getId())
//                                .name(post.getCategory().getName())
//                                .build())
//                        .build())
//                .toList();
//
//        return ResponseEntity.ok(dtoList);
//    }
@GetMapping("/posts/user/{username}")
public ResponseEntity<List<UserPostsDTO>> getPostsByUsername(@PathVariable String username) {

    // Pobranie użytkownika po username
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found with username: " + username));

    // Pobranie postów użytkownika
    List<Post> userPosts = postRepository.findByUserId(user.getId());

    // Mapowanie Post -> UserPostsDTO z użyciem buildera
    List<UserPostsDTO> dtoList = userPosts.stream()
            .map(post -> UserPostsDTO.builder()
                    .id(post.getId())
                    .userId(user.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .category(CategoryDTO.builder()
                            .id(post.getCategory().getId())
                            .name(post.getCategory().getName())
                            .build())
                    .build())
            .toList();

    return ResponseEntity.ok(dtoList);
}
}
