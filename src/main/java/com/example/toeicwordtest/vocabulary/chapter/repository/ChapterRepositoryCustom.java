package com.example.toeicwordtest.vocabulary.chapter.repository;

import com.example.toeicwordtest.vocabulary.chapter.entity.Chapter;

import java.util.Optional;

public interface ChapterRepositoryCustom {
    Optional<Chapter> findChapterWithWordsById(Long chapterId);
}
