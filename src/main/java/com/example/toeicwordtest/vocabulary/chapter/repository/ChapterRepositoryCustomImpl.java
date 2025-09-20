package com.example.toeicwordtest.vocabulary.chapter.repository;

import com.example.toeicwordtest.vocabulary.chapter.entity.Chapter;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.example.toeicwordtest.vocabulary.chapter.entity.QChapter.chapter;
import static com.example.toeicwordtest.vocabulary.word.entity.QWord.word;
import static com.example.toeicwordtest.auth.user.entity.QUser.user;


@Repository
@RequiredArgsConstructor
public class ChapterRepositoryCustomImpl implements ChapterRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Chapter> findChapterWithWordsById(Long chapterId) {
        Chapter foundChapter = queryFactory
                .selectFrom(chapter)
                .leftJoin(chapter.user, user).fetchJoin()
                .leftJoin(chapter.words, word).fetchJoin()
                .where(chapter.id.eq(chapterId))
                .distinct() // 중복 제거 (Fetch Join으로 인한 데이터 뻥튀기 방지)
                .fetchOne();

        return Optional.ofNullable(foundChapter);
    }
}