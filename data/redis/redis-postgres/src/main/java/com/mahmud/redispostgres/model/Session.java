package com.mahmud.redispostgres.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("sessions")  // This tells Spring Data Redis to store this entity in the "sessions" hash.
public class Session {

    @Id  // Marks this field as the primary key in Redis.
    private String sessionId;

    private Long userId;  // Foreign key to the PostgreSQL User entity.

    private LocalDateTime loginTime;

    private LocalDateTime lastActiveTime;
}