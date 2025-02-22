package com.mahmud.gridfsfileuploading.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class GridFSConfig {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Bean
    public GridFSBucket gridFSBucket() {
        // Get the database name from MongoTemplate or hardcode it if necessary
        String databaseName = mongoTemplate.getDb().getName();
        // Create and return the GridFSBucket bean
        return GridFSBuckets.create(mongoClient.getDatabase(databaseName));
    }
}