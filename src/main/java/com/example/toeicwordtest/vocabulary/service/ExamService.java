package com.example.toeicwordtest.vocabulary.service;

import com.example.toeicwordtest.auth.user.entity.User;
import com.example.toeicwordtest.vocabulary.dto.ExamForm;
import com.example.toeicwordtest.vocabulary.dto.ExamResultDto;
import com.example.toeicwordtest.vocabulary.dto.WordDto;
import com.example.toeicwordtest.vocabulary.mapper.VocabularyMapper;
import com.example.toeicwordtest.vocabulary.domain.word.entity.Word;
import com.example.toeicwordtest.vocabulary.domain.word.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamService {

    private final WordRepository wordRepository;
    private final WrongNoteService wrongNoteService;
    private final VocabularyMapper vocabularyMapper;

    /**
     * 어떤 단어 목록이든 받아서 시험 문제를 준비(셔플, DTO 변환)하는 공통 메소드
     */
    public List<WordDto> prepareExam(List<Word> wordsForExam) {
        if (wordsForExam == null || wordsForExam.isEmpty()) {
            return Collections.emptyList();
        }
        List<WordDto> wordDtos = wordsForExam.stream()
                .map(vocabularyMapper::toWordDto)
                .collect(Collectors.toList());
        Collections.shuffle(wordDtos);
        return wordDtos;
    }

    /**
     * 시험 답안을 제출받아 채점하고, 오답노트를 관리하는 메소드
     */
    @Transactional
    public ExamResultDto submitExam(User user, ExamForm examForm) {
        List<Long> wordIds = examForm.getWordIds();
        Map<Long, String> userAnswers = examForm.getUserAnswers();

        if (wordIds == null || wordIds.isEmpty()) {
            throw new IllegalArgumentException("채점할 단어가 없습니다.");
        }

        List<Word> examWords = wordRepository.findAllById(wordIds);
        Map<Long, Word> wordMap = examWords.stream().collect(Collectors.toMap(Word::getId, word -> word));

        int correctAnswers = 0;
        List<ExamResultDto.WrongAnswerDetail> wrongAnswerDetails = new ArrayList<>();

        for (Long wordId : wordIds) {
            Word word = wordMap.get(wordId);
            if (word == null) continue;

            String userAnswer = userAnswers.getOrDefault(wordId, "").trim();
            String correctAnswer = examForm.getExamType().equalsIgnoreCase("SPELLING") ?
                    word.getSpelling().trim() : word.getMeaning().trim();
            boolean isCorrect = userAnswer.equalsIgnoreCase(correctAnswer);

            if (isCorrect) {
                correctAnswers++;
                // ★ 맞췄을 경우, 오답노트에서 해당 단어를 삭제
                wrongNoteService.removeWrongNoteEntry(user, word);
            } else {
                // ★ 틀렸을 경우, 오답노트에 해당 단어를 추가
                wrongNoteService.addWrongNoteEntry(user, word);
                wrongAnswerDetails.add(new ExamResultDto.WrongAnswerDetail(
                        word.getId(), word.getSpelling(), word.getMeaning(), userAnswer, correctAnswer));
            }
        }

        return ExamResultDto.builder()
                .totalQuestions(wordIds.size())
                .correctAnswers(correctAnswers)
                .wrongAnswers(wordIds.size() - correctAnswers)
                .examType(examForm.getExamType())
                .wrongAnswerDetails(wrongAnswerDetails)
                .build();
    }
}