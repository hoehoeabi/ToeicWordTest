package com.example.toeicwordtest.vocabulary.chapter.entity;

import com.example.toeicwordtest.auth.user.entity.User;
import com.example.toeicwordtest.vocabulary.word.entity.Word;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "chapter", uniqueConstraints = { // ★ 유니크 제약 조건 추가 (user_id와 chapter_number의 조합)
        @UniqueConstraint(columnNames = {"user_id", "chapterNumber"})
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chapter {

    @Id
    @Column(name = "chapter_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // unique = true 제거
    private int chapterNumber;

    @Column(nullable = false, length = 100) // ★ 챕터 제목 필드 추가 (nullable = false로 필수)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // user_id도 필수 (챕터는 유저에게 속해야 함)
    private User user;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Word> words = new ArrayList<>();

    // == 연관 관계 편의 메서드 == //
    // Chapter가 ManyToOne의 주인이므로, User 엔티티를 설정하는 메서드는 setUser가 적절합니다.
    public void setUser(User user) {
        if (this.user != user) {
            // 기존 User와의 관계 끊기 (User 엔티티에 removeChapter 메서드를 추가할 수도 있음)
            if (this.user != null) {
                this.user.getChapters().remove(this); // 기존 유저 컬렉션에서 자신을 제거
            }
            this.user = user; // 새로운 User로 설정
            if (user != null && !user.getChapters().contains(this)) {
                user.addChapter(this); // 새 유저 컬렉션에 자신을 추가
            }
        }
    }

    public void addWord(Word word) {
        if (word != null) { // null 체크
            if (!this.words.contains(word)) { // 중복 확인
                this.words.add(word);
            }
            if (word.getChapter() != this) { // 양방향 관계 설정
                word.setChapter(this);
            }
        }
    }

    public void removeWord(Word word) {
        if (word != null && this.words.contains(word)) {
            this.words.remove(word);
            // orphanRemoval = true 덕분에 DB에서 Word 엔티티는 자동으로 삭제됩니다.
            // 하지만 객체 상의 관계를 끊기 위해 Word 쪽에서도 chapter를 null로 설정하는 것이 명시적입니다.
            word.setChapter(null);
        }
    }

    // == 비즈니스 로직 메서드 == //
    /**
     * 챕터의 세부 정보를 업데이트합니다. (chapterNumber와 title 변경)
     */
    public void updateChapterDetails(int chapterNumber, String title) {
        this.chapterNumber = chapterNumber;
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chapter chapter = (Chapter) o;
        // id가 null이 아니고, 두 객체의 id가 동일하면 같은 객체로 판단
        return id != null && id.equals(chapter.id);
    }

    @Override
    public int hashCode() {
        // id를 기반으로 해시코드 생성
        return Objects.hash(id);
    }

}