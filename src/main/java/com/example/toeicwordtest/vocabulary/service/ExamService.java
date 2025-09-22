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
        // 시험에 사용된 단어들 id
        List<Long> wordIds = examForm.getWordIds();
        // 사용자 답안 목록
        Map<Long, String> userAnswers = examForm.getUserAnswers();

        if (wordIds == null || wordIds.isEmpty()) {
            throw new IllegalArgumentException("채점할 단어가 없습니다.");
        }

        // 시험에 사용된 단어들 id로 db에서 가져옴(정답 비교를 위해)
        List<Word> examWords = wordRepository.findAllById(wordIds);
        // 위에서 가져온 단어들 id를 키로 값으로 Word엔티티 자체를 넣어줌
        Map<Long, Word> wordMap = examWords.stream().collect(Collectors.toMap(Word::getId, word -> word));

        int correctAnswers = 0;
        // 채점을 위한 리스트
        List<ExamResultDto.WrongAnswerDetail> wrongAnswerDetails = new ArrayList<>();

        // 시험에 사용된 단어들 id를 하나씩 꺼냄
        for (Long wordId : wordIds) {
            // 디비에서 가져온 단어 자체 즉 정답을 넣어줌
            Word word = wordMap.get(wordId);
            if (word == null) continue;

            // 단어 id를 기반으로 map에서 사용자가 입력한 정답, db에서 가져온 단어와 비교 함
            String userAnswer = userAnswers.getOrDefault(wordId, "").trim();
            String correctAnswer = examForm.getExamType().equalsIgnoreCase("SPELLING") ?
                    word.getSpelling().trim() : word.getMeaning().trim();
            boolean isCorrect = userAnswer.equalsIgnoreCase(correctAnswer);

            if (isCorrect) {
                correctAnswers++;
                //  맞췄을 경우, 오답노트에서 해당 단어를 삭제
                wrongNoteService.removeWrongNoteEntry(user, word);
            } else {
                //  틀렸을 경우, 오답노트에 해당 단어를 추가
                wrongNoteService.addWrongNoteEntry(user, word);
                // 사용자에게 보여줄 틀린단어 및 입력한 값 보여주기 위함
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