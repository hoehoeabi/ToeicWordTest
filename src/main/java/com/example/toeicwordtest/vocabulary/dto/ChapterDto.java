package com.example.toeicwordtest.vocabulary.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterDto {

    private Long id; // 챕터 수정 시 필요 (ID가 있으면 수정, 없으면 등록)
    private int chapterNumber;
    private String title;

    private List<WordDto> words; // 챕터 생성/수정 시 함께 전달될 단어 목록
    private int wordCount;


    // '틀린 단어' 가상 챕터를 생성하는 정적 팩토리 메소드
    public static ChapterDto createWrongNoteChapter(int count) {
        return ChapterDto.builder()
                .id(0L)
                .chapterNumber(0)
                .title("틀린 단어 모음")
                .wordCount(count) // ★ wordCount 필드에 직접 개수 설정
                .words(new ArrayList<>()) // 단어 목록은 비워둠
                .build();
    }
}