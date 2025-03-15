package com.mahmud.openfeign.service;

import com.mahmud.openfeign.client.PostClient;
import com.mahmud.openfeign.dto.Post;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    private final PostClient postClient;

    public PostService(PostClient postClient) {
        this.postClient = postClient;
    }

    public List<Post> getAllPosts() {
        return postClient.getPosts();
    }

    public Post getPostById(Integer id) {
        return postClient.getPostById(id);
    }

    public Post createPost(Post post) {
        return postClient.createPost(post);
    }

    public Post updatePost(Integer id, Post post) {
        return postClient.updatePost(id, post);
    }

    public void deletePost(Integer id) {
        postClient.deletePost(id);
    }
}
