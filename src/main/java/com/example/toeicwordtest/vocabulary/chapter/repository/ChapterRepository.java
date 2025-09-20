// src/main/java/com/example/toeicwordtest/vocabulary/chapter/repository/ChapterRepository.java
package com.example.toeicwordtest.vocabulary.chapter.repository;

import com.example.toeicwordtest.auth.user.entity.User;
import com.example.toeicwordtest.vocabulary.chapter.entity.Chapter;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long>, ChapterRepositoryCustom { // ★ ChapterRepositoryCustom 상속 추가

    // 특정 유저의 챕터 번호로 챕터 조회 (중복 확인 및 단일 챕터 조회 시 사용)
    Optional<Chapter> findByUserAndChapterNumber(User user, int chapterNumber);

    // 특정 유저의 모든 챕터 목록 조회 (챕터 홈 화면 등에서 사용)
    @EntityGraph(attributePaths = "words") // words 컬렉션을 Fetch Join 하라는 의미
    List<Chapter> findByUserOrderByChapterNumberAsc(User user);

    // ID와 User로 챕터 조회 (보안 강화: 해당 유저의 챕터인지 확인)
    @EntityGraph(attributePaths = "words") // words 컬렉션을 Fetch Join
    Optional<Chapter> findByIdAndUser(Long chapterId, User user);

    // 여러 ID와 유저로 챕터 목록을 조회 (words는 Fetch Join)
    @EntityGraph(attributePaths = "words")
    List<Chapter> findByIdInAndUser(List<Long> ids, User user);
}