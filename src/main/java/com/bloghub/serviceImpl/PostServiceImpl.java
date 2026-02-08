package com.bloghub.serviceImpl;

import java.util.List;

import org.springframework.data.domain.Page;

import com.bloghub.request.payload.dto.PostUpdateDTO;
import com.bloghub.request.payload.dto.PostcreateDTO;
import com.bloghub.response.payload.dto.PostResponseDTO;
import com.bloghub.service.PostService;

public class PostServiceImpl implements PostService{

	@Override
	public PostResponseDTO postCreate(PostcreateDTO req) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<PostResponseDTO> getAllPosts(int page, int size, String sortBy, String sortDir) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PostResponseDTO> searchPosts(String term) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PostResponseDTO> getPostsByAuthor(Long authorId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PostResponseDTO postUpdate(PostUpdateDTO req, Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PostResponseDTO> getAllPosts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PostResponseDTO postgetById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void postDelete(Long id) {
		// TODO Auto-generated method stub
		
	}

}
