package com.example.toeicwordtest.domain.wrongnote.entity;

import com.example.toeicwordtest.domain.user.entity.User;
import com.example.toeicwordtest.domain.word.entity.Word;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "wrong_note_entry") // 새로운 테이블 이름
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WrongNoteEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wrong_note_entry_id")
    private Long id;

    // 누가 이 단어를 틀렸는지 (User와 N:1 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 어떤 단어를 틀렸는지 (Word와 N:1 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    // 틀린 날짜 (나중에 추가를 고려할 기능)
    @Column(name = "wrong_date", nullable = false)
    @Builder.Default
    private LocalDateTime wrongDate = LocalDateTime.now();

    // 틀린 횟수 등 추가 정보
    // private int wrongCount = 1;
}