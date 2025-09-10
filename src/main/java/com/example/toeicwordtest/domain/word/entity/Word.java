package com.example.toeicwordtest.domain.word.entity;

import com.example.toeicwordtest.domain.chapter.entity.Chapter;
import com.example.toeicwordtest.domain.wrongnote.entity.WrongNote;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "word")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "word_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String spelling;

    @Column(nullable = false)
    private String meaning;

    // 여러 개의 Word는 하나의 Chapter에 속합니다. (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id") // 'word' 테이블에 'chapter_id' 외래 키(FK)를 생성합니다.
    private Chapter chapter;

    // 이 단어는 여러 개의 오답노트에 포함될 수 있습니다.
    // 'words'는 WrongNote 엔티티의 Set<Word> 필드 이름입니다.
    @ManyToMany(mappedBy = "words")
    private Set<WrongNote> wrongNotes = new HashSet<>();
}
