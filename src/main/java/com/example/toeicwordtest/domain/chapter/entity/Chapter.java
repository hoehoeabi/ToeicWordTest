package com.example.toeicwordtest.domain.chapter.entity;

import com.example.toeicwordtest.domain.user.entity.User;
import com.example.toeicwordtest.domain.word.entity.Word;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chapter")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chapter {

    @Id
    @Column(name = "chapter_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int chapterNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 'chapter' 테이블에 'user_id' 외래 키(FK)를 생성합니다.
    private User user;


    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Word> words = new ArrayList<>();

    // == 연관 관계 편의 메서드 == //

    public void addUser(User user) {
        //  Chapter의 user 필드에 User를 설정
        if (this.user != user) { // 이미 같은 User라면 아무것도 하지 않음 (무한 루프 방지)
            // 기존 User와의 관계 끊기 (선택 사항: 양방향 제거 로직) 아마 이런 케이스는 없을거임
            if (this.user != null) {
                this.user.getChapters().remove(this);
            }
            this.user = user; // 새로운 User로 설정
        }

        // 2. User의 chapters 컬렉션에 Chapter를 추가 (User.addChapter() 호출)
        //    이때 User.addChapter() 내부에서 다시 Chapter.setUser()를 호출하지 않도록 방지 로직 필요
        if (user != null && !user.getChapters().contains(this)) { // User가 아직 이 Chapter를 가지고 있지 않다면
            //user.getChapters().add(this); // User가 자신의 chapters 컬렉션에 Chapter를 추가
            user.addChapter(this);
        }
    }



    public void addWord(Word word) {
        if (word != null && !this.words.contains(word)) {
            this.words.add(word);
            if (word.getChapter() != this) {
                word.setChapter(this);
            }
        }
    }
}
