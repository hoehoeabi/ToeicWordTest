package com.example.toeicwordtest.vocabulary.wrongnote.repository;

import com.example.toeicwordtest.auth.user.entity.User;
import com.example.toeicwordtest.vocabulary.word.entity.Word;
import com.example.toeicwordtest.vocabulary.wrongnote.entity.WrongNoteEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
// import java.util.Optional; // Optional 임포트 필요 없음

@Repository
public interface WrongNoteEntryRepository extends JpaRepository<WrongNoteEntry, Long> {

    List<WrongNoteEntry> findByUser(User user);

    @Query("SELECT wne FROM WrongNoteEntry wne JOIN FETCH wne.word WHERE wne.user = :user")
    List<WrongNoteEntry> findByUserFetchJoinWord(@Param("user") User user);

    // 특정 유저의 오답노트를 가져오는
    Optional<WrongNoteEntry> findByUserAndWord(User user, Word word);

    // 특정 유저의 오답노트 개수를 세는 쿼리
    long countByUser(User user);

    // 특정 유저와 단어에 대한 오답 기록이 존재하는지 확인
    boolean existsByUserAndWord(User user, Word word);

}