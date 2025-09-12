package com.example.toeicwordtest.domain.user.entity;

import com.example.toeicwordtest.domain.chapter.entity.Chapter;
import com.example.toeicwordtest.domain.role.entity.Role;
import com.example.toeicwordtest.domain.wrongnote.entity.WrongNoteEntry;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter @Setter
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
    @Builder.Default
    private List<Chapter> chapters = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default // Builder 사용 시 초기화 누락 방지
    private List<WrongNoteEntry> wrongNoteEntries = new ArrayList<>();
}
