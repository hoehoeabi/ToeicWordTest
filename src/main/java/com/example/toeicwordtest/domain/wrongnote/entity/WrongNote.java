package com.example.toeicwordtest.domain.wrongnote.entity;

import com.example.toeicwordtest.domain.user.entity.User;
import com.example.toeicwordtest.domain.word.entity.Word;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "wrong_note")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WrongNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wrong_note_id")
    private Long id;

    // 이 오답노트의 주인인 유저와 1:1 관계입니다.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true) // user_id FK, 유니크 제약조건으로 1:1 관계를 보장합니다.
    private User user;

    // 이 오답노트는 여러 개의 단어를 포함할 수 있습니다. (N:M)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "wrong_note_words", // 중간 연결 테이블의 이름
            joinColumns = @JoinColumn(name = "wrong_note_id"), // 이 엔티티(WrongNote)를 참조하는 FK
            inverseJoinColumns = @JoinColumn(name = "word_id") // 반대편 엔티티(Word)를 참조하는 FK
    )
    private Set<Word> words = new HashSet<>();
}
