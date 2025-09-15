package com.example.toeicwordtest.domain.user.entity;

import com.example.toeicwordtest.domain.chapter.entity.Chapter;
import com.example.toeicwordtest.domain.role.entity.Role;
import com.example.toeicwordtest.domain.wrongnote.entity.WrongNoteEntry;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(name = "registrationDate", nullable = false)
    @Builder.Default
    private LocalDateTime registrationDate  = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // mappedBy에는 조인할 테이블에서 조인할 객체?의 변수명을 적는거임
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    @Builder.Default // 이게 있어야 빌더 만들때 값을 안넣은 상태에서 get해도 null이 아니라 빈 ArrayList가 나옴
    private List<Chapter> chapters = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default // Builder 사용 시 초기화 누락 방지
    private List<WrongNoteEntry> wrongNoteEntries = new ArrayList<>();

    /**
     * 닉네임 변경
     */
    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
    }

    /**
     * 비밀번호 변경 (암호화 로직 포함)
     */
    public void changePassword(String newPassword, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(newPassword);
    }

    // == 연관 관계 편의 메서드 == //

    public void addChapter(Chapter chapter) {
        //  User의 chapters 컬렉션에 Chapter를 추가
        if (!this.chapters.contains(chapter)) { // 이미 포함되어 있는지 확인하여 무한 루프 방지
            this.chapters.add(chapter);
        }
        // 2. Chapter의 user 필드에 User를 설정 (Chapter.setUser() 호출)
        //    이때 Chapter.setUser() 내부에서 다시 User.addChapter()를 호출하지 않도록 방지 로직 필요
        if (chapter.getUser() != this) { // Chapter가 아직 이 User를 참조하고 있지 않다면
            chapter.addUser(this); // Chapter가 자신의 user 필드를 설정 (이때 Chapter.setUser의 내부 로직이 중요)
        }
    }

    /**
     * User가 WrongNoteEntry를 추가할 때 사용합니다.
     */

    public void addWrongNoteEntry(WrongNoteEntry wrongNoteEntry) {
        if (wrongNoteEntry != null && !this.wrongNoteEntries.contains(wrongNoteEntry)) {
            // 이 User의 wrongNoteEntries 리스트에 WrongNoteEntry를 추가
            this.wrongNoteEntries.add(wrongNoteEntry);
            // WrongNoteEntry가 관계의 주인이므로 WrongNoteEntry 쪽에서도 관계를 설정해줘야 함
            // WrongNoteEntry의 setUser 메서드 안에서 이 User의 컬렉션에 자신을 추가하는 로직 포함
            if (wrongNoteEntry.getUser() != this) {
                wrongNoteEntry.setUser(this);
            }
        }
    }
}
