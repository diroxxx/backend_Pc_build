//package org.example.backend_pcbuild.Community.Service;
//
//import jakarta.persistence.EntityNotFoundException;
//import lombok.AllArgsConstructor;
//import org.example.backend_pcbuild.Community.DTO.PostImageDTO;
//import org.example.backend_pcbuild.Community.Models.Post;
//import org.example.backend_pcbuild.Community.Models.PostImage;
//import org.example.backend_pcbuild.Community.Repository.PostImageRepository;
//import org.example.backend_pcbuild.Community.Repository.PostRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@Transactional(readOnly = true)
//@AllArgsConstructor
//public class PostImageService {
//
//    private static final int MAX_IMAGES_PER_POST = 10;
//    private final PostRepository postRepository;
//    private final PostImageRepository postImageRepository;
//
//    @Transactional
//    public PostImageDTO addImageToPost(
//            Long postId,
//            byte[] imageData,
//            String mimeType,
//            String originalFilename) {
//
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new EntityNotFoundException("Post nie znaleziony o ID: " + postId));
//
//        if (post.getImages().size() >= MAX_IMAGES_PER_POST) {
//            throw new IllegalStateException("Osiągnięto maksymalny limit " + MAX_IMAGES_PER_POST + " zdjęć dla tego postu.");
//        }
//
//        PostImage newImage = new PostImage();
//        newImage.setImage(imageData);
//        newImage.setMimeType(mimeType);
//        newImage.setFilename(originalFilename);
//        newImage.setPost(post);
//
//        PostImage savedImage = postImageRepository.save(newImage);
//
//        return new PostImageDTO(savedImage.getId(), savedImage.getFilename(), savedImage.getMimeType());
//    }
//
//    public PostImage getImageById(Long imageId) {
//        return postImageRepository.findById(imageId)
//                .orElseThrow(() -> new EntityNotFoundException("Obraz nie znaleziony o ID: " + imageId));
//    }
//}

package org.project.backend_pcbuild.community.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.project.backend_pcbuild.community.dto.PostImageDTO;
import org.project.backend_pcbuild.community.model.Post;
import org.project.backend_pcbuild.community.model.PostImage;
import org.project.backend_pcbuild.community.repository.PostImageRepository;
import org.project.backend_pcbuild.community.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class PostImageService {

    private static final int MAX_IMAGES_PER_POST = 10;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    @Transactional
    public PostImageDTO addImageToPost(
            Long postId,
            byte[] imageData,
            String mimeType,
            String originalFilename) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post nie znaleziony o ID: " + postId));

        if (post.getImages() != null && post.getImages().size() >= MAX_IMAGES_PER_POST) {
            throw new IllegalStateException("Osiągnięto maksymalny limit " + MAX_IMAGES_PER_POST + " zdjęć dla tego postu.");
        }

        PostImage newImage = new PostImage();
        newImage.setImage(imageData);
        newImage.setMimeType(mimeType);
        newImage.setFilename(originalFilename);
        newImage.setPost(post);

        PostImage savedImage = postImageRepository.save(newImage);

        return new PostImageDTO(savedImage.getId(), savedImage.getFilename(), savedImage.getMimeType());
    }

    public PostImage getImageById(Long imageId) {
        return postImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Obraz nie znaleziony o ID: " + imageId));
    }
}