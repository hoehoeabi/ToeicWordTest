package com.example.toeicwordtest.vocabulary.controller;

import com.example.toeicwordtest.auth.user.entity.User;
import com.example.toeicwordtest.auth.user.repository.UserRepository;
import com.example.toeicwordtest.global.security.CustomUserDetails;
import com.example.toeicwordtest.vocabulary.dto.ChapterDto;
import com.example.toeicwordtest.vocabulary.dto.ExamForm;
import com.example.toeicwordtest.vocabulary.dto.ExamResultDto;
import com.example.toeicwordtest.vocabulary.dto.WordDto;
import com.example.toeicwordtest.vocabulary.service.ChapterService;
import com.example.toeicwordtest.vocabulary.service.ExamService;
import com.example.toeicwordtest.vocabulary.domain.word.entity.Word;
import com.example.toeicwordtest.vocabulary.service.WrongNoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vocabulary/exam")
@Slf4j
public class ExamController {

    private final ChapterService chapterService;
    private final ExamService examService;
    private final WrongNoteService wrongNoteService;
    private final UserRepository userRepository;

    // 1. 시험 시작 페이지 (시험 종류 선택)
    @GetMapping("/start")
    public String showExamStartPage() {
        return "vocabulary/examStart";
    }

    // 2. 시험 종류 선택 후 -> 챕터 선택 페이지로 이동
    @PostMapping("/select-chapters")
    public String showChapterSelectionPage(@AuthenticationPrincipal CustomUserDetails currentUser,
                                           @RequestParam("examType") String examType, Model model) {
        Long userId = currentUser.getId();
        List<ChapterDto> chapters = chapterService.getAllChaptersForUser(userId);

        // 오답노트에 단어가 있으면 '틀린 단어' 가상 챕터 추가
        // 챕터 리스트 페이지를 조회 할때마다 디비에서 틀린단어 여부를 조회 함
        // 레디스 같은거로 캐싱하지 않는 이번 프로젝트 특성상 데이터 정합성을 위해 어쩔 수 없음
        int wrongWordsCount = wrongNoteService.getWrongWordsCount(userId);
        if (wrongWordsCount > 0) {
            chapters.add(0, ChapterDto.createWrongNoteChapter(wrongWordsCount));
        }

        model.addAttribute("chapters", chapters);
        model.addAttribute("examType", examType); // 선택한 시험 유형을 다음 페이지로 전달
        return "vocabulary/chapterSelection";
    }

    // 3. 챕터 선택 후 -> 시험 페이지로 이동
    @PostMapping("/start-test")
    public String startTest(@AuthenticationPrincipal CustomUserDetails currentUser,
                            @RequestParam("examType") String examType,
                            @RequestParam(value = "selectedChapterIds", required = false) List<Long> selectedChapterIds,
                            Model model, RedirectAttributes redirectAttributes) {

        if (selectedChapterIds == null || selectedChapterIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "최소 하나 이상의 챕터를 선택해야 합니다.");
            // examType을 다시 전달하여 챕터 선택 페이지로 돌아감
            redirectAttributes.addAttribute("examType", examType);
            return "redirect:/vocabulary/exam/select-chapters-form"; // GET 요청으로 리다이렉트
        }

        Long userId = currentUser.getId();
        List<Word> wordsForExam = new ArrayList<>();

        // '틀린 단어' 챕터(ID=0)가 선택되었는지 확인
        if (selectedChapterIds.contains(0L)) {
            wordsForExam.addAll(wrongNoteService.getWordsFromWrongNote(userId));
            selectedChapterIds.remove(0L); // 실제 챕터 ID 목록에서는 제거
        }

        // 나머지 실제 챕터들의 단어 가져오기
        if (!selectedChapterIds.isEmpty()) {
            wordsForExam.addAll(chapterService.getWordsFromChapters(userId, selectedChapterIds));
        }

        // 중복 제거 (오답노트 단어가 일반 챕터에도 속할 경우 대비)
        List<Word> distinctWords = wordsForExam.stream().distinct().collect(Collectors.toList());
        List<WordDto> examWords = examService.prepareExam(distinctWords);

        if (examWords.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "선택된 챕터에 단어가 없습니다.");
            redirectAttributes.addAttribute("examType", examType);
            return "redirect:/vocabulary/exam/select-chapters-form";
        }

        model.addAttribute("words", examWords);
        model.addAttribute("examType", examType); // 시험 페이지로 examType 전달
        return "vocabulary/examPage";
    }

    // 챕터 선택 페이지를 GET으로도 접근할 수 있도록 form 핸들러 추가
    @GetMapping("/select-chapters-form")
    public String showChapterSelectionFormRedirect(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                   @RequestParam("examType") String examType, Model model) {
        // PostMapping의 로직과 동일
        return showChapterSelectionPage(currentUser, examType, model);
    }


    // 4. 시험 답안 제출 및 채점
    @PostMapping("/submit-test")
    public String submitTest(@AuthenticationPrincipal CustomUserDetails currentUser,
                             @ModelAttribute ExamForm examForm,
                             RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        ExamResultDto result = examService.submitExam(user, examForm);
        // 시험결과를 리다이렉트 할때 같이 보내줌(showExamResultPage의 매개변수를 보면 이해됨)
        redirectAttributes.addFlashAttribute("examResult", result);
        return "redirect:/vocabulary/exam/result";
    }

    // 5. 시험 결과 페이지
    @GetMapping("/result")
    public String showExamResultPage(@ModelAttribute("examResult") ExamResultDto examResult, Model model) {
        if (examResult == null || examResult.getTotalQuestions() == 0) {
            return "redirect:/vocabulary/exam/start";
        }
        model.addAttribute("examResult", examResult);
        return "vocabulary/examResult";
    }

    // 틀린 단어 테스트
    @PostMapping("/start-wrong-note-test")
    public String startWrongNoteTest(@AuthenticationPrincipal CustomUserDetails currentUser,
                                     @RequestParam("examType") String examType,
                                     Model model) {
        Long userId = currentUser.getId();
        List<Word> words = wrongNoteService.getWordsFromWrongNote(userId);
        List<WordDto> examWords = examService.prepareExam(words);

        model.addAttribute("words", examWords);
        model.addAttribute("examType", examType);
        return "vocabulary/examPage";
    }
}