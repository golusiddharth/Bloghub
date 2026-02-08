package com.bloghub.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bloghub.request.payload.dto.PostUpdateDTO;
import com.bloghub.request.payload.dto.PostcreateDTO;
import com.bloghub.response.payload.dto.PostResponseDTO;
import com.bloghub.service.PostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    //CREATE POST (author from JWT)
    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(
          @Valid  @RequestBody PostcreateDTO req) {

        PostResponseDTO response = postService.postCreate(req);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // GET ALL POSTS (no pagination)
    @GetMapping("/all")
    public ResponseEntity<List<PostResponseDTO>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    // GET ALL POSTS WITH PAGINATION
    @GetMapping
    public ResponseEntity<Page<PostResponseDTO>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        return ResponseEntity.ok(
                postService.getAllPosts(page, size, sortBy, sortDir)
        );
    }

    //  GET POST BY ID
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> getPostById(
            @PathVariable Long id) {

        return ResponseEntity.ok(postService.postgetById(id));
    }

    //SEARCH POSTS
    @GetMapping("/search")
    public ResponseEntity<List<PostResponseDTO>> searchPosts(
            @RequestParam String term) {

        return ResponseEntity.ok(postService.searchPosts(term));
    }

    //  GET POSTS BY AUTHOR
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<PostResponseDTO>> getPostsByAuthor(
            @PathVariable Long authorId) {

        return ResponseEntity.ok(postService.getPostsByAuthor(authorId));
    }

    //  UPDATE POST (only owner – JWT)
    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable Long id,
            @RequestBody PostUpdateDTO req) {

        return ResponseEntity.ok(postService.postUpdate(req, id));
    }

    //  DELETE POST (only owner – JWT)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(
            @PathVariable Long id) {

        postService.postDelete(id);
        return ResponseEntity.ok("Post deleted successfully");
    }
}
