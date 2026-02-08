package com.bloghub.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.bloghub.request.payload.dto.PostUpdateDTO;
import com.bloghub.request.payload.dto.PostcreateDTO;
import com.bloghub.response.payload.dto.PostResponseDTO;

public interface PostService {
      
	PostResponseDTO postCreate(PostcreateDTO req);
	public Page<PostResponseDTO> getAllPosts(int page, int size, String sortBy, String sortDir);
	public List<PostResponseDTO> searchPosts(String term);
	public List<PostResponseDTO> getPostsByAuthor(Long authorId);
	PostResponseDTO postUpdate(PostUpdateDTO req,Long id);	
	public List<PostResponseDTO> getAllPosts();
	PostResponseDTO postgetById(Long id);
	void postDelete(Long id);
          
}
