package com.example.toeicwordtest.vocabulary.domain.chapter.repository;

import com.example.toeicwordtest.vocabulary.domain.chapter.entity.Chapter;

import java.util.Optional;

public interface ChapterRepositoryCustom {

    // Fetch Join을 사용하여 챕터와 단어를 한 번에 가져오는 쿼리 (N+1 방지)
    Optional<Chapter> findChapterWithWordsById(Long chapterId);
}
