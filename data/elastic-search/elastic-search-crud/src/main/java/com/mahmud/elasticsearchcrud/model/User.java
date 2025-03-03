package com.mahmud.elasticsearchcrud.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;


@Document(indexName = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @NotNull(message = "ID cannot be null")
    private Integer id;

    @NotNull(message = "Name cannot be null")
    private String name;

    @Email(message = "Email must be a valid email address")
    @NotNull(message = "Email cannot be null")
    private String email;
}