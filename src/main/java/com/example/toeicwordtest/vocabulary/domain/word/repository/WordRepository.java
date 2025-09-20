package com.example.toeicwordtest.vocabulary.domain.word.repository;

import com.example.toeicwordtest.vocabulary.domain.word.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
    Optional<Word> findBySpelling(String spelling);

}
