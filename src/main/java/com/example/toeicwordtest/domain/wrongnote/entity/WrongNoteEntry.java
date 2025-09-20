package com.example.toeicwordtest.domain.wrongnote.entity;

import com.example.toeicwordtest.domain.user.entity.User;
import com.example.toeicwordtest.domain.word.entity.Word;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "wrong_note_entry") // 새로운 테이블 이름
@Getter
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

    // == 연관 관계 편의 메서드 == //

    /**
     * WrongNoteEntry에 User를 설정하고, 양방향 관계의 일관성을 맞춥니다.
     * @param user 새로운 User 객체
     */
    public void setUser(User user) {
        if (this.user != user) { // 이미 같은 User라면 아무것도 하지 않음 (무한 루프 방지)
            // 기존 User와의 관계 끊기 (선택 사항: 양방향 제거 로직, 여기서는 보통 변경이 없으니 생략 가능)
            if (this.user != null) {
                this.user.getWrongNoteEntries().remove(this);
            }
            this.user = user; // 새로운 User로 설정

            // 새로운 User에 자신을 추가 (새로운 User가 null이 아닐 경우)
            if (user != null) {
                user.getWrongNoteEntries().add(this);
            }
        }
    }

    /**
     * WrongNoteEntry에 Word를 설정하고, 양방향 관계의 일관성을 맞춥니다.
     * @param word 새로운 Word 객체
     */
    public void setWord(Word word) {
        if (this.word != word) { // 이미 같은 Word라면 아무것도 하지 않음 (무한 루프 방지)
            // 기존 Word와의 관계 끊기
            if (this.word != null) {
                this.word.getWrongNoteEntries().remove(this);
            }
            this.word = word; // 새로운 Word로 설정

            // 새로운 Word에 자신을 추가 (새로운 Word가 null이 아닐 경우)
            if (word != null) {
                word.addWrongNoteEntry(this);
            }
        }
    }
}