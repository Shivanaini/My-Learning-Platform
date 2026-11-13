package com.learning.lecture.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Stores uploaded lecture files on the local disk (folder configured by app.storage.location). */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "mp4", "webm", "ogg", "mov", "mkv", "m4v",
            "mp3", "wav", "m4a",
            "pdf", "ppt", "pptx", "doc", "docx", "txt",
            "png", "jpg", "jpeg", "gif", "zip");

    private final Path root;

    public FileStorageService(@Value("${app.storage.location}") String location) {
        this.root = Paths.get(location).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(root);
    }

    /** Saves the file and returns the generated stored file name. */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please choose a file to upload");
        }
        String ext = extension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File type '." + ext + "' is not allowed. Allowed: " + ALLOWED_EXTENSIONS);
        }
        String storedName = UUID.randomUUID() + "." + ext;
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, root.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save file", e);
        }
        return storedName;
    }

    public Resource load(String storedName) {
        try {
            Path file = root.resolve(storedName).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (!file.startsWith(root) || !resource.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecture file not found");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecture file not found");
        }
    }

    public void delete(String storedName) {
        try {
            Files.deleteIfExists(root.resolve(storedName).normalize());
        } catch (IOException ignored) {
            // metadata is removed anyway; an orphaned file is harmless
        }
    }

    private static String extension(String filename) {
        String ext = StringUtils.getFilenameExtension(filename);
        return ext == null ? "" : ext.toLowerCase(Locale.ROOT);
    }
}
