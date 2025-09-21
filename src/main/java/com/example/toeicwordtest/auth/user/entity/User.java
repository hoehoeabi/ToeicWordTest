package com.example.toeicwordtest.auth.user.entity;

import com.example.toeicwordtest.vocabulary.domain.chapter.entity.Chapter;
import com.example.toeicwordtest.auth.role.entity.Role;
import com.example.toeicwordtest.vocabulary.domain.wrongnote.entity.WrongNoteEntry;
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

    @Column(name = "registration_date", nullable = false)
    @Builder.Default
    private LocalDateTime registrationDate  = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * '연관관계의 주인이 아님'을 명시.
     * 이 관계의 주인은 '다(N)'쪽인 Chapter 엔티티이며, Chapter 엔티티 안에 있는 'user' 필드가 이 관계를 관리.
     * mappedBy = "user"는 "Chapter 엔티티의 'user' 필드에 의해 매핑되었다"는 의미.
     *
     * [키 전략] 비즈니스적으로 (어떤 유저, 어떤 챕터 번호)는 챕터를 식별하는 '자연 키(복합 키)'가 될 수 있다.
     * 하지만 JPA에서 복합 키를 기본 키(@Id)로 사용하면 @IdClass 등을 만들어야 해서 복잡해지므로,
     * 보통 간단한 '대리 키(Surrogate Key)'(예: Long id)를 기본 키로 사용.
     *
     * [영속성 전이] cascade = CascadeType.ALL은 User의 상태 변화(저장, 삭제 등)를 Chapter에도 전파시키는 설정입니다.
     * 이는 위에서 설명한 키 전략과는 별개의 개념.
     *
     * '고아 객체 제거' 기능. CascadeType.REMOVE와는 다름.
     * - CascadeType.REMOVE: 부모(User)가 삭제되면 자식(Chapter)도 함께 삭제.
     * - orphanRemoval=true: 부모의 컬렉션에서 자식을 제거하면 (예: user.getChapters().remove(chapter)),
     * 부모를 잃은 자식(고아)이 되어 DB에서도 삭제.
     */
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
            chapter.setUser(this); // Chapter가 자신의 user 필드를 설정 (이때 Chapter.setUser의 내부 로직이 중요)
        }
    }

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
