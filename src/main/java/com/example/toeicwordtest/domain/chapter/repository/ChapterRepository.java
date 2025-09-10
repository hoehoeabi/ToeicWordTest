package com.example.toeicwordtest.domain.chapter.repository;

import com.example.toeicwordtest.domain.chapter.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    Optional<Chapter> chapterNumber(int chapterNumber);

    Optional<Chapter> findByChapterNumber(int number);
}
