package com.example.toeicwordtest.vocabulary.wrongnote.entity;

import com.example.toeicwordtest.auth.user.entity.User;
import com.example.toeicwordtest.vocabulary.word.entity.Word;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "wrong_note_entry")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WrongNoteEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wrong_note_entry_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;


    // === 연관 관계 편의 메서드 (ExamService에서 직접 설정하므로, 여기서 제거해도 무방) ===
    public void setUser(User user) {
        if (this.user != user) {
            if (this.user != null) {
                this.user.getWrongNoteEntries().remove(this);
            }
            this.user = user;
            if (user != null) {
                user.getWrongNoteEntries().add(this);
            }
        }
    }

    public void setWord(Word word) {
        if (this.word != word) {
            if (this.word != null) {
                this.word.getWrongNoteEntries().remove(this);
            }
            this.word = word;
            if (word != null) {
                 word.addWrongNoteEntry(this);
            }
        }
    }



}