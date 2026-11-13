package com.learning.lecture.service;

import com.learning.lecture.dto.LectureResponse;
import com.learning.lecture.model.Lecture;
import com.learning.lecture.repository.LectureRepository;
import com.learning.lecture.security.AuthUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LectureService {

    private final LectureRepository repository;
    private final FileStorageService storage;

    public LectureService(LectureRepository repository, FileStorageService storage) {
        this.repository = repository;
        this.storage = storage;
    }

    public List<LectureResponse> list(String q, String course) {
        return repository.search(blankToNull(q), blankToNull(course)).stream()
                .map(LectureResponse::from).toList();
    }

    public List<String> courses() {
        return repository.findDistinctCourses();
    }

    public Lecture get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecture " + id + " not found"));
    }

    @Transactional
    public LectureResponse upload(MultipartFile file, String title, String description, String course, AuthUser user) {
        requireInstructor(user);
        if (!StringUtils.hasText(title)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        String storedName = storage.store(file);
        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? storedName : file.getOriginalFilename());

        Lecture lecture = new Lecture();
        lecture.setTitle(title.trim());
        lecture.setDescription(description == null ? null : description.trim());
        lecture.setCourse(blankToNull(course == null ? null : course.trim()));
        lecture.setStoredFileName(storedName);
        lecture.setOriginalFileName(originalName);
        lecture.setContentType(resolveContentType(file, originalName));
        lecture.setFileSize(file.getSize());
        lecture.setUploadedBy(user.username());
        lecture.setUploadedByName(user.fullName());
        return LectureResponse.from(repository.save(lecture));
    }

    @Transactional
    public void delete(Long id, AuthUser user) {
        requireInstructor(user);
        Lecture lecture = get(id);
        repository.delete(lecture);
        storage.delete(lecture.getStoredFileName());
    }

    private void requireInstructor(AuthUser user) {
        if (user == null || !user.canManageLectures()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only instructors can manage lectures");
        }
    }

    /** Browsers often send application/octet-stream; fall back to guessing from the file name. */
    private static String resolveContentType(MultipartFile file, String name) {
        String ct = file.getContentType();
        if (ct == null || ct.isBlank() || ct.equals("application/octet-stream")) {
            ct = MediaTypeFactory.getMediaType(name).map(Object::toString).orElse("application/octet-stream");
        }
        return ct;
    }

    private static String blankToNull(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }
}
