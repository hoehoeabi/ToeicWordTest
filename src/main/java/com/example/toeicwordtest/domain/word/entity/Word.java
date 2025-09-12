package com.example.toeicwordtest.domain.word.entity;

import com.example.toeicwordtest.domain.chapter.entity.Chapter;
import com.example.toeicwordtest.domain.wrongnote.entity.WrongNoteEntry;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    @Column(nullable = false)
    private String spelling;

    @Column(nullable = false)
    private String meaning;

    // 여러 개의 Word는 하나의 Chapter에 속합니다. (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id") // 'word' 테이블에 'chapter_id' 외래 키(FK)를 생성합니다.
    private Chapter chapter;

    // 이 단어는 여러 개의 오답 기록에 포함될 수 있습니다. (1:N 관계의 N쪽에 해당)
    // mappedBy = "word"는 WrongNoteEntry 엔티티의 'word' 필드를 참조합니다.
    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default // Builder 사용 시 초기화 누락 방지
    private List<WrongNoteEntry> wrongNoteEntries = new ArrayList<>();
}