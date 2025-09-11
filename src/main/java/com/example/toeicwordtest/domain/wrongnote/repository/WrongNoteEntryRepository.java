package com.example.toeicwordtest.domain.wrongnote.repository;

import com.example.toeicwordtest.domain.user.entity.User;
import com.example.toeicwordtest.domain.wrongnote.entity.WrongNoteEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WrongNoteEntryRepository extends JpaRepository<WrongNoteEntry, Long> {
    // 특정 유저의 모든 오답 단어 기록을 조회
    List<WrongNoteEntry> findByUser(User user);

    //  Fetch Join을 사용한 메소드
    @Query("SELECT wne FROM WrongNoteEntry wne JOIN FETCH wne.word WHERE wne.user = :user")
    List<WrongNoteEntry> findByUserFetchJoinWord(@Param("user") User user);

    // 특정 유저가 특정 단어를 틀린 기록이 있는지 조회
    Optional<WrongNoteEntry> findByUserAndWord(User user, com.example.toeicwordtest.domain.word.entity.Word word);

    //  특정 유저의 오답노트에서 특정 단어 삭제
    void deleteByUserAndWord(User user, com.example.toeicwordtest.domain.word.entity.Word word);
}
