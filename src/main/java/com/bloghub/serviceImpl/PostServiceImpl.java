package com.bloghub.serviceImpl;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bloghub.domain.UserRole;
import com.bloghub.entity.*;
import com.bloghub.exception.ResourceNotFoundException;
import com.bloghub.mapper.PostMapper;
import com.bloghub.repository.*;
import com.bloghub.request.payload.dto.*;
import com.bloghub.response.payload.dto.PostResponseDTO;
import com.bloghub.service.PostService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository     postRepository;
    private final UserRepository     userRepository;
    private final CategoryRepository categoryRepository;

    // application.properties se upload directory path lega
    @Value("${app.upload.dir:${user.home}/bloghub-uploads}")
    private String uploadDir;

    // ── Get JWT logged-in user ──
    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal().equals("anonymousUser"))
            throw new RuntimeException("Unauthorized");
        String email = auth.getName();
        return Optional.ofNullable(userRepository.findByEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // ── Save image ──
    private String saveImage(MultipartFile file) {
        try {
            // Validate type
            String contentType = file.getContentType();
            if (contentType == null ||
               (!contentType.equals("image/jpeg") &&
                !contentType.equals("image/png")  &&
                !contentType.equals("image/webp") &&
                !contentType.equals("image/gif"))) {
                throw new RuntimeException("Only JPG, PNG, WEBP, GIF images are allowed.");
            }

            // Max 5 MB
            if (file.getSize() > 5 * 1024 * 1024)
                throw new RuntimeException("Image size must be under 5 MB.");

            // Upload folder: {uploadDir}/images/
            Path uploadPath = Paths.get(uploadDir, "images");
            if (!Files.exists(uploadPath))
                Files.createDirectories(uploadPath);

            // Unique filename
            String originalName = file.getOriginalFilename();
            String extension    = (originalName != null && originalName.contains("."))
                                  ? originalName.substring(originalName.lastIndexOf("."))
                                  : ".jpg";
            String fileName = System.currentTimeMillis() + "_"
                            + UUID.randomUUID().toString().substring(0, 8)
                            + extension;

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // URL path jo frontend use karega: /uploads/images/filename.jpg
            return "/uploads/images/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage());
        }
    }

    @Override
    public PostResponseDTO postCreate(PostcreateDTO req, MultipartFile image) {

        User loggedInUser = getLoggedInUser();

        if (loggedInUser.getRole() == UserRole.ADMIN)
            throw new RuntimeException("Admin is not allowed to create post");

        if (req.getAuthorID() == null || !req.getAuthorID().equals(loggedInUser.getId()))
            throw new RuntimeException("Give correct author id");

        Category category = categoryRepository.findById(req.getCategoryID())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Post post = new Post();
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setCreatedAt(LocalDateTime.now());
        post.setAuthor(loggedInUser);
        post.setCategory(category);

        // Image save karo agar diya hai
        if (image != null && !image.isEmpty()) {
            String imageUrl = saveImage(image);
            post.setImageUrl(imageUrl);
        }

        return PostMapper.toDto(postRepository.save(post));
    }

    @Override
    public List<PostResponseDTO> getAllPosts() {
        return postRepository.findAll()
                .stream()
                .map(PostMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PostResponseDTO> getAllPosts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        Page<Post> postPage  = postRepository.findAll(pageable);
        List<PostResponseDTO> dtoList = postPage.getContent()
                .stream()
                .map(PostMapper::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, postPage.getTotalElements());
    }

    @Override
    public PostResponseDTO postgetById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return PostMapper.toDto(post);
    }

    @Override
    public List<PostResponseDTO> searchPosts(String term) {
        List<Post> posts = postRepository
                .findByTitleContainingOrContentContaining(term, term);
        List<PostResponseDTO> responseList = new ArrayList<>();
        for (Post post : posts) responseList.add(PostMapper.toDto(post));
        return responseList;
    }

    @Override
    public List<PostResponseDTO> getPostsByAuthor(Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));
        return postRepository.findByAuthor(author)
                .stream()
                .map(PostMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PostResponseDTO postUpdate(PostUpdateDTO req, Long id) {
        User loggedInUser = getLoggedInUser();
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        if (!post.getAuthor().getId().equals(loggedInUser.getId()))
            throw new RuntimeException("You can update only your post");
        if (req.getTitle()      != null) post.setTitle(req.getTitle());
        if (req.getContent()    != null) post.setContent(req.getContent());
        if (req.getCategoryID() != null) {
            Category category = categoryRepository.findById(req.getCategoryID())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            post.setCategory(category);
        }
        return PostMapper.toDto(postRepository.save(post));
    }

    @Override
    public void postDelete(Long id) {
        User loggedInUser = getLoggedInUser();
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        if (loggedInUser.getRole() == UserRole.ADMIN) {
            postRepository.delete(post); return;
        }
        if (loggedInUser.getRole() == UserRole.AUTHOR &&
            post.getAuthor().getId().equals(loggedInUser.getId())) {
            postRepository.delete(post); return;
        }
        throw new RuntimeException("You are not allowed to delete this post");
    }
}