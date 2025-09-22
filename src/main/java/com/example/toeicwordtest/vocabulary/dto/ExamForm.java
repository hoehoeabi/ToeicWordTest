package com.example.toeicwordtest.vocabulary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamForm {
    private String examType; // "SPELLING" (철자 맞추기) 또는 "MEANING" (뜻 맞추기)
    private List<Long> wordIds; // 시험에 사용된 단어들의 ID 목록
    private Map<Long, String> userAnswers; // key: wordId, value: 사용자 답변
}