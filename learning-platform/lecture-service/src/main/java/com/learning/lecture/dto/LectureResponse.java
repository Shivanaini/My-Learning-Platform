package com.learning.lecture.dto;

import com.learning.lecture.model.Lecture;

import java.time.Instant;

public record LectureResponse(
        Long id,
        String title,
        String description,
        String course,
        String fileName,
        String contentType,
        String mediaType,
        long fileSize,
        String uploadedBy,
        String uploadedByName,
        Instant uploadedAt,
        String contentUrl,
        String downloadUrl) {

    public static LectureResponse from(Lecture l) {
        return new LectureResponse(
                l.getId(), l.getTitle(), l.getDescription(), l.getCourse(),
                l.getOriginalFileName(), l.getContentType(), mediaType(l.getContentType()),
                l.getFileSize(), l.getUploadedBy(), l.getUploadedByName(), l.getUploadedAt(),
                "/api/lectures/" + l.getId() + "/content",
                "/api/lectures/" + l.getId() + "/download");
    }

    /** Simple category the UI uses to decide how to preview the file. */
    private static String mediaType(String contentType) {
        if (contentType == null) return "OTHER";
        if (contentType.startsWith("video/")) return "VIDEO";
        if (contentType.startsWith("audio/")) return "AUDIO";
        if (contentType.equals("application/pdf")) return "PDF";
        if (contentType.startsWith("image/")) return "IMAGE";
        return "DOCUMENT";
    }
}
