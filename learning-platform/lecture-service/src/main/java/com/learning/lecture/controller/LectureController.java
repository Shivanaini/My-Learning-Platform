package com.learning.lecture.controller;

import com.learning.lecture.dto.LectureResponse;
import com.learning.lecture.model.Lecture;
import com.learning.lecture.security.AuthUser;
import com.learning.lecture.service.FileStorageService;
import com.learning.lecture.service.LectureService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/lectures")
public class LectureController {

    private final LectureService lectures;
    private final FileStorageService storage;

    public LectureController(LectureService lectures, FileStorageService storage) {
        this.lectures = lectures;
        this.storage = storage;
    }

    /** List lectures, optionally filtered by search text and course. */
    @GetMapping
    public List<LectureResponse> list(@RequestParam(required = false) String q,
                                      @RequestParam(required = false) String course) {
        return lectures.list(q, course);
    }

    @GetMapping("/courses")
    public List<String> courses() {
        return lectures.courses();
    }

    @GetMapping("/{id}")
    public LectureResponse get(@PathVariable Long id) {
        return LectureResponse.from(lectures.get(id));
    }

    /** Upload a lecture (multipart/form-data). Instructors only. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public LectureResponse upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam("title") String title,
                                  @RequestParam(value = "description", required = false) String description,
                                  @RequestParam(value = "course", required = false) String course,
                                  @RequestAttribute(AuthUser.REQUEST_ATTRIBUTE) AuthUser user) {
        return lectures.upload(file, title, description, course, user);
    }

    /** Streams the file inline (supports HTTP Range so videos can seek). */
    @GetMapping("/{id}/content")
    public ResponseEntity<Resource> content(@PathVariable Long id) {
        return fileResponse(lectures.get(id), ContentDisposition.inline());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        return fileResponse(lectures.get(id), ContentDisposition.attachment());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @RequestAttribute(AuthUser.REQUEST_ATTRIBUTE) AuthUser user) {
        lectures.delete(id, user);
    }

    private ResponseEntity<Resource> fileResponse(Lecture lecture, ContentDisposition.Builder disposition) {
        Resource resource = storage.load(lecture.getStoredFileName());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(lecture.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition
                        .filename(lecture.getOriginalFileName(), StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(resource);
    }
}
