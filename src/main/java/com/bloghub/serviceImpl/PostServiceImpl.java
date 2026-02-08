package com.bloghub.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.bloghub.domain.UserRole;
import com.bloghub.entity.Category;
import com.bloghub.entity.Post;
import com.bloghub.entity.User;
import com.bloghub.exception.ResourceNotFoundException;
import com.bloghub.mapper.PostMapper;
import com.bloghub.repository.CategoryRepository;
import com.bloghub.repository.PostRepository;
import com.bloghub.repository.UserRepository;
import com.bloghub.request.payload.dto.PostUpdateDTO;
import com.bloghub.request.payload.dto.PostcreateDTO;
import com.bloghub.response.payload.dto.PostResponseDTO;
import com.bloghub.service.PostService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    //  JWT se logged-in user
    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || auth.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("Unauthorized");
        }

        String email = auth.getName();
        return Optional
                .ofNullable(userRepository.findByEmail(email))
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    @Override
    public PostResponseDTO postCreate(PostcreateDTO req) {

        User loggedInUser = getLoggedInUser();

        Category category = categoryRepository.findById(req.getCategoryID())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        //  Admin cannot create post
        if (loggedInUser.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Admin is not allowed to create post");
        }

        //  Author ID validation (optional but strict)
        if (req.getAuthorID() == null ||
            !req.getAuthorID().equals(loggedInUser.getId())) {
            throw new RuntimeException("Give correct author id");
        }
        Post post = new Post();
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setCreatedAt(LocalDateTime.now());
        post.setAuthor(loggedInUser);
        post.setCategory(category);

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
        Page<Post> postPage = postRepository.findAll(pageable);

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

    	    List<Post> posts =
    	            postRepository.findByTitleContainingOrContentContaining(term, term);

    	    List<PostResponseDTO> responseList = new ArrayList<>();

    	    for (Post post : posts) {
    	        PostResponseDTO dto = PostMapper.toDto(post);
    	        responseList.add(dto);
    	    }

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

        //  same authorization logic
        if (!post.getAuthor().getId().equals(loggedInUser.getId())) {
            throw new RuntimeException("You can update only your post");
        }

        if (req.getTitle() != null) post.setTitle(req.getTitle());
        if (req.getContent() != null) post.setContent(req.getContent());

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

        //  ADMIN can delete any post
        if (loggedInUser.getRole() == UserRole.ADMIN) {
            postRepository.delete(post);
            return;
        }

        //  AUTHOR can delete only his own post
        if (loggedInUser.getRole() == UserRole.AUTHOR &&
            post.getAuthor().getId().equals(loggedInUser.getId())) {

            postRepository.delete(post);
            return;
        }

        //  Not allowed
        throw new RuntimeException("You are not allowed to delete this post");
    }

}
