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
@Setter
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
}
