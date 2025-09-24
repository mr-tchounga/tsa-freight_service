package com.shifter.freight_service.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.shifter.freight_service.exceptions.FileNotFoundException;
import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.shifter.freight_service.repositories.FileRepository;
import com.shifter.freight_service.models.Files;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    @Autowired
    private FileRepository fileRepository;

//    private final String FILE_PATH = "C:\\AuthUserResponses\\DIGITAL MARKET\\Data";
    private final String FILE_PATH = Paths.get("").toAbsolutePath().toString() + File.separator + "Data";

    public FileService() { createDataDirectory(); }

    // Method to create the Data directory if it doesn't exist
    private void createDataDirectory() {
        File dataDir = new File(FILE_PATH);
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs(); // Create the directory
            if (created) {
                log.info("Data directory created at: " + FILE_PATH);
            } else {
                log.error("Failed to create Data directory at: " + FILE_PATH);
            }
        }
    }
    
    
    public String storeFile(MultipartFile file, AuthUserResponse user) throws IOException {
        Files files = Files
                .builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .imageData(file.getBytes())
                .userId(user.getId()).build();

        files = fileRepository.save(files);
		log.info("File uploaded successfully into database with id {} for user {}", files.getId(), user.getId());
        if (files.getId() != null) {
            return "File uploaded successfully into databse";
        }
        return null;
    }

	public String storeDataIntoFileSystem(MultipartFile file, AuthUserResponse user) throws IOException {
		String filePath = FILE_PATH + '\\' + file.getOriginalFilename();

		Files files = Files.builder().name(file.getOriginalFilename()).path(filePath).type(file.getContentType())
				.imageData(file.getBytes()).userId(user.getId()).build();

		files = fileRepository.save(files);
		file.transferTo(new File(filePath));
		if (files.getId() != null) {
			return "File uploaded successfully into database for user with id " + user.getId();
		}

		return null;
	}


    @Transactional(readOnly = true)
    public List<Files> getFiles(Long userId) {
        return fileRepository.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Files getFileById(Long fileId, Long userId) {
        return fileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new FileNotFoundException("File not found for user or access denied: " + fileId));
    }

    @Transactional(readOnly = true)
    public byte[] getFileBytesFromDb(Long fileId, AuthUserResponse user) {
        Files file = getFileById(fileId, user.getId());
        return file.getImageData();
    }

    @Transactional(readOnly = true)
    public byte[] getFileBytesFromFileSystem(Long fileId, AuthUserResponse user) throws IOException {
        Files f = getFileById(fileId, user.getId());
        String pathStr = f.getPath();
        if (pathStr == null) {
            throw new FileNotFoundException("Path not set for file: " + fileId);
        }
        Path path = Paths.get(pathStr);
        if (!java.nio.file.Files.exists(path)) {
            throw new FileNotFoundException("File not found on disk: " + pathStr);
        }
        return java.nio.file.Files.readAllBytes(path);
    }
}
