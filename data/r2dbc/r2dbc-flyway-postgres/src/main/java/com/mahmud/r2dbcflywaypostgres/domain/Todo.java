package com.mahmud.r2dbcflywaypostgres.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("todos")
public class Todo {
    @Id
    private Long id;
    private String title;
    private String description;
    private Boolean completed = false;
    private LocalDateTime createdAt;
    private Integer priority = 0; // New field
}