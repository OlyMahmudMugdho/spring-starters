package com.mahmud.basicfileupload.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RestController
public class FileUploadController {

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "File is empty!";
        }
        return "File uploaded: " + file.getOriginalFilename();
    }

    @PostMapping("/upload-temp")
    public String handleTempFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            // Generate a temporary file path
            Path tempDir = Files.createTempDirectory("spring-upload-");
            Path tempFile = tempDir.resolve(file.getOriginalFilename());

            // Save the file
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            return "File saved temporarily: " + tempFile;
        } catch (Exception e) {
            return "Upload failed: " + e.getMessage();
        }
    }
}