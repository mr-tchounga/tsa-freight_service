package com.shifter.freight_service.controllers;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.shifter.freight_service.clients.AuthServiceClient;
import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.shifter.freight_service.models.Files;
import com.shifter.freight_service.services.FileService;

@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin
public class FileController {
	
	private static final Logger log = LoggerFactory.getLogger(FileService.class);
	
	@Autowired
	private FileService fileService;
    @Autowired
    private AuthServiceClient client;

	
	@PostMapping("/db")
	public ResponseEntity<String> storeFilesIntoDB(@RequestHeader("Authorization") String authHeader,
                                                   @RequestParam("file") MultipartFile file) throws IOException {
        AuthUserResponse user = client.getCurrentUser(authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(fileService.storeFile(file, user));
	}

    @PostMapping("/system")
    public ResponseEntity<String> uploadFileIntoFileSystem(@RequestHeader("Authorization") String authHeader,
                                                           @RequestParam("file") MultipartFile file) throws IOException {
        AuthUserResponse user = client.getCurrentUser(authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(fileService.storeDataIntoFileSystem(file, user));
    }


    @GetMapping()
    public ResponseEntity<List<Files>> getAllFiles(@RequestHeader("Authorization") String authHeader) {
        List<Files> list = fileService.getFiles(client.getCurrentUser(authHeader).getId());
        return ResponseEntity.ok(list);
    }

	
	@GetMapping("/db/{fileId}")
    public ResponseEntity<byte[]> getFile(@RequestHeader("Authorization") String authHeader, @PathVariable Long fileId) {
        AuthUserResponse user = client.getCurrentUser(authHeader);
        Files file = fileService.getFileById(fileId, user.getId());
        byte[] data = fileService.getFileBytesFromDb(fileId, user);

        String contentType = file.getType();
        MediaType mediaType;
        try {
            mediaType = (contentType != null && !contentType.isBlank())
                    ? MediaType.parseMediaType(contentType)
                    : MediaType.APPLICATION_OCTET_STREAM;
        } catch (Exception ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        // Decide inline preview vs forced download. Use inline for browser preview:
        String disposition = "inline"; // change to "attachment" to force download
        String filename = StringUtils.hasText(file.getName()) ? file.getName() : "file";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + encodedFilename + "\"")
                .body(data);
    }

	
	@GetMapping("/system/{fileId}")
    public ResponseEntity<byte[]> downloadFileFromFileSystem(@RequestHeader("Authorization") String authHeader, @PathVariable Long fileId) throws IOException {
        AuthUserResponse user = client.getCurrentUser(authHeader);
        Files file = fileService.getFileById(fileId, user.getId());
        byte[] data = fileService.getFileBytesFromFileSystem(fileId, user);

        String contentType = file.getType();
        MediaType mediaType;
        try {
            mediaType = (contentType != null && !contentType.isBlank())
                    ? MediaType.parseMediaType(contentType)
                    : MediaType.APPLICATION_OCTET_STREAM;
        } catch (Exception ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        // If you want the browser to download always:
        String disposition = "attachment";
        String filename = file.getName() != null ? file.getName() : "file";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + encodedFilename + "\"")
                .body(data);
    }

}
