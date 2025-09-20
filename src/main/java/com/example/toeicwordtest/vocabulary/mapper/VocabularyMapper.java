package com.example.toeicwordtest.vocabulary.mapper;

import com.example.toeicwordtest.vocabulary.chapter.entity.Chapter;
import com.example.toeicwordtest.vocabulary.dto.ChapterDto;
import com.example.toeicwordtest.vocabulary.dto.WordDto;
import com.example.toeicwordtest.vocabulary.word.entity.Word;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VocabularyMapper {

    public ChapterDto toChapterDto(Chapter chapter) {
        List<WordDto> wordDtos = chapter.getWords().stream()
                .map(this::toWordDto) // toWordDto 메소드 재사용
                .collect(Collectors.toList());

        return ChapterDto.builder()
                .id(chapter.getId())
                .chapterNumber(chapter.getChapterNumber())
                .title(chapter.getTitle())
                .words(wordDtos)
                .wordCount(wordDtos.size())
                .build();
    }

    public WordDto toWordDto(Word word) {
        if (word == null) return null;
        return WordDto.builder()
                .id(word.getId())
                .spelling(word.getSpelling())
                .meaning(word.getMeaning())
                .build();
    }
}