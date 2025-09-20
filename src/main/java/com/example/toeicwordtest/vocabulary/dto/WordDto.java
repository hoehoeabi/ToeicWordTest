package com.example.toeicwordtest.vocabulary.dto;

import com.example.toeicwordtest.vocabulary.word.entity.Word; // Word 엔티티 임포트
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordDto {
    private Long id;
    private String spelling;
    private String meaning;

}