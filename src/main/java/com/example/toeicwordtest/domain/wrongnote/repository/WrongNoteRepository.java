package com.example.toeicwordtest.domain.wrongnote.repository;

import com.example.toeicwordtest.domain.wrongnote.entity.WrongNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WrongNoteRepository extends JpaRepository<WrongNote, Long> {

}
