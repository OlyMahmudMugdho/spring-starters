package com.mahmud.elasticsearchcrud.repository;

import com.mahmud.elasticsearchcrud.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserRepository extends ElasticsearchRepository<User, Integer> {
    Page<User> findAll(Pageable pageable);
}
