package com.example.toeicwordtest.vocabulary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResultDto {
    private int totalQuestions;
    private int correctAnswers;
    private int wrongAnswers;
    private String examType; // SPELLING or MEANING
    private List<WrongAnswerDetail> wrongAnswerDetails; // 틀린 문제 상세 정보

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WrongAnswerDetail {
        private Long wordId;
        private String spelling;
        private String meaning;
        private String userAnswer;
        private String correctAnswer;
    }
}