package com.mahmud.openfeign.client;

import com.mahmud.openfeign.dto.Post;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "postClient", url = "https://jsonplaceholder.typicode.com")
public interface PostClient {
    @GetMapping("/posts")
    List<Post> getPosts();

    @GetMapping("/posts/{id}")
    Post getPostById(@PathVariable("id") Integer id);

    @PostMapping("/posts")
    Post createPost(@RequestBody Post post);

    @PutMapping("/posts/{id}")
    Post updatePost(@PathVariable("id") Integer id, @RequestBody Post post);

    @DeleteMapping("/posts/{id}")
    void deletePost(@PathVariable("id") Integer id);
}
