package com.example.toeicwordtest.vocabulary.word.entity;

import com.example.toeicwordtest.vocabulary.chapter.entity.Chapter;
import com.example.toeicwordtest.vocabulary.wrongnote.entity.WrongNoteEntry;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "word")
@Getter
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WrongNoteEntry> wrongNoteEntries = new ArrayList<>();

    // == 연관 관계 편의 메서드 == //
    public void setChapter(Chapter chapter) {
        if (this.chapter != chapter) {
            if (this.chapter != null) {
                this.chapter.getWords().remove(this); // 기존 챕터에서 자신을 제거
            }
            this.chapter = chapter;
            if (chapter != null && !chapter.getWords().contains(this)) {
                chapter.getWords().add(this); // 새 챕터에 자신을 추가
            }
        }
    }

    public void addWrongNoteEntry(WrongNoteEntry wrongNoteEntry) {
        if (wrongNoteEntry != null) {
            if (!this.wrongNoteEntries.contains(wrongNoteEntry)) {
                this.wrongNoteEntries.add(wrongNoteEntry);
            }
            if (wrongNoteEntry.getWord() != this) {
                wrongNoteEntry.setWord(this);
            }
        }
    }

// == 비즈니스 로직 메서드 == //
    /**
     * 단어의 철자와 의미를 업데이트합니다.
     */
    public void updateWordDetails(String spelling, String meaning) {
        this.spelling = spelling;
        this.meaning = meaning;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return id != null && id.equals(word.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}