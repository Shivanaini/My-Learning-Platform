package com.learning.lecture.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "lectures")
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    /** Course / subject the lecture belongs to, e.g. "Java Basics". */
    @Column(length = 100)
    private String course;

    /** Name of the file on disk (UUID-based, never user-controlled). */
    @Column(nullable = false, unique = true)
    private String storedFileName;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false, length = 150)
    private String contentType;

    private long fileSize;

    @Column(nullable = false, length = 50)
    private String uploadedBy;

    @Column(length = 100)
    private String uploadedByName;

    @Column(nullable = false)
    private Instant uploadedAt = Instant.now();

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String storedFileName) { this.storedFileName = storedFileName; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public String getUploadedByName() { return uploadedByName; }
    public void setUploadedByName(String uploadedByName) { this.uploadedByName = uploadedByName; }
    public Instant getUploadedAt() { return uploadedAt; }
}
