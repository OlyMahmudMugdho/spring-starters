package com.mahmud.gridfsfileuploading.controller;

import com.mahmud.gridfsfileuploading.service.GridFsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class FileUploadController {

    private final GridFsService gridFsService;

    public FileUploadController(GridFsService gridFsService) {
        this.gridFsService = gridFsService;
    }

    @PostMapping("/upload-gridfs")
    public String handleGridFsUpload(@RequestParam("file") MultipartFile file) {
        try {
            String fileId = gridFsService.storeFile(file);
            return "File stored in GridFS with ID: " + fileId;
        } catch (IOException e) {
            return "Upload failed: " + e.getMessage();
        }
    }
}
