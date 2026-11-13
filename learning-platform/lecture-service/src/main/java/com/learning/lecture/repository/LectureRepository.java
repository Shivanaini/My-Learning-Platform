package com.learning.lecture.repository;

import com.learning.lecture.model.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

    @Query("""
            SELECT l FROM Lecture l
            WHERE (:course IS NULL OR l.course = :course)
              AND (:q IS NULL
                   OR LOWER(l.title) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(l.description) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY l.uploadedAt DESC
            """)
    List<Lecture> search(@Param("q") String q, @Param("course") String course);

    @Query("SELECT DISTINCT l.course FROM Lecture l WHERE l.course IS NOT NULL ORDER BY l.course")
    List<String> findDistinctCourses();
}
