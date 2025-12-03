package org.example.backend_pcbuild.Community.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Community.Models.Post;
import org.example.backend_pcbuild.Community.Models.SavedPost;
import org.example.backend_pcbuild.Community.Repository.PostRepository;
import org.example.backend_pcbuild.UserProfile.SavedPostRepository;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.models.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedPostService {


    private final SavedPostRepository savedPostRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public SavedPost savePost(Long userId, Long postId) {
        // 1. Walidacja istnienia posta i użytkownika
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie znaleziony."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post nie znaleziony."));

        // 2. Walidacja, czy post nie jest już zapisany (dzięki unikalnemu kluczowi, baza danych też to sprawdzi, ale to lepsze dla UX)
        if (savedPostRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new IllegalStateException("Post jest już zapisany przez tego użytkownika.");
        }

        // 3. Tworzenie i zapis nowej encji SavedPost
        SavedPost savedPost = new SavedPost(user, post);
        return savedPostRepository.save(savedPost);
    }


    @Transactional
    public void unsavePost(Long userId, Long postId) {
        SavedPost savedPost = savedPostRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> new RuntimeException("Post nie został zapisany lub nie znaleziono relacji."));

        savedPostRepository.delete(savedPost);
    }


    @Transactional
    public List<Post> getSavedPosts(Long userId) {
        List<SavedPost> savedPosts = savedPostRepository.findAllByUserId(userId);


        return savedPosts.stream()
                .map(SavedPost::getPost)
                // Upewnij się, że używasz user() i category()
                .peek(post -> {
                    if (post.getUser() != null) post.getUser().getId();
                    if (post.getCategory() != null) post.getCategory().getId();
                })
                .collect(Collectors.toList());
    }

    public boolean isPostSaved(Long userId, Long postId) {
        return savedPostRepository.existsByUserIdAndPostId(userId, postId);
    }

}
