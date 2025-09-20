package com.example.toeicwordtest.vocabulary.controller;

import com.example.toeicwordtest.global.security.CustomUserDetails;
import com.example.toeicwordtest.vocabulary.dto.ChapterDto;
import com.example.toeicwordtest.vocabulary.service.ChapterService;
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

@Controller
@RequiredArgsConstructor
@RequestMapping("/vocabulary")
@Slf4j
public class VocabularyController {

    private final ChapterService chapterService;
    private final WrongNoteService wrongNoteService;

    // --- 챕터 목록 조회 (오답노트 챕터 포함) ---
    @GetMapping("/chapters")
    public String listChapters(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        Long userId = currentUser.getId();
        List<ChapterDto> chapters = chapterService.getAllChaptersForUser(userId);

        // 오답노트에 단어가 1개 이상 있을 경우, '틀린 단어' 가상 챕터를 목록에 추가
        int wrongWordsCount = wrongNoteService.getWrongWordsCount(userId);
        if (wrongWordsCount > 0) {
            chapters.add(0, ChapterDto.createWrongNoteChapter(wrongWordsCount));
        }

        model.addAttribute("chapters", chapters);
        return "vocabulary/chapterList";
    }

    // --- 챕터 생성 폼 ---
    @GetMapping("/chapters/new")
    public String showCreateChapterForm(Model model) {
        ChapterDto chapterDto = new ChapterDto();
        chapterDto.setWords(new ArrayList<>()); //  비어있는 words 리스트를 초기화해주는 부분
        model.addAttribute("chapter", chapterDto);
        return "vocabulary/chapterForm";
    }

    // --- 챕터 수정 폼 ---
    @GetMapping("/chapters/{chapterId}/edit")
    public String showEditChapterForm(@AuthenticationPrincipal CustomUserDetails currentUser,
                                      @PathVariable("chapterId") Long chapterId, Model model) {
        Long userId = currentUser.getId();
        ChapterDto chapterDto = chapterService.getChapter(userId, chapterId);
        model.addAttribute("chapter", chapterDto);
        return "vocabulary/chapterForm";
    }

    // --- 챕터 등록 및 수정 처리 (try-catch 제거) ---
    @PostMapping("/chapters")
    public String saveOrUpdateChapter(@AuthenticationPrincipal CustomUserDetails currentUser,
                                      @ModelAttribute("chapter") ChapterDto chapterDto,
                                      RedirectAttributes redirectAttributes) {
        Long userId = currentUser.getId();
        ChapterDto result;
        String message;

        if (chapterDto.getId() == null) {
            result = chapterService.createChapter(userId, chapterDto);
            message = "챕터가 성공적으로 등록되었습니다.";
        } else {
            result = chapterService.updateChapter(userId, chapterDto.getId(), chapterDto);
            message = "챕터가 성공적으로 수정되었습니다.";
        }

        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/vocabulary/chapters/" + result.getId();
    }

    // --- 챕터 상세 조회 ---
    @GetMapping("/chapters/{chapterId}")
    public String viewChapter(@AuthenticationPrincipal CustomUserDetails currentUser,
                              @PathVariable("chapterId") Long chapterId, Model model) {
        Long userId = currentUser.getId();
        ChapterDto chapter = chapterService.getChapter(userId, chapterId);
        model.addAttribute("chapter", chapter);
        return "vocabulary/chapterDetail";
    }

    // --- 틀린 단어 목록(가상 챕터) 상세 조회 ---
    @GetMapping("/wrong-notes")
    public String viewWrongNotes(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        Long userId = currentUser.getId();
        ChapterDto wrongNoteChapter = wrongNoteService.getWrongNoteChapter(userId);
        model.addAttribute("chapter", wrongNoteChapter);
        // chapterDetail.html을 재사용하되, 수정/삭제 버튼이 없도록 처리
        return "vocabulary/chapterDetail";
    }

    // --- 챕터 삭제 ---
    @PostMapping("/chapters/{chapterId}/delete")
    public String deleteChapter(@AuthenticationPrincipal CustomUserDetails currentUser,
                                @PathVariable("chapterId") Long chapterId,
                                RedirectAttributes redirectAttributes) {
        Long userId = currentUser.getId();
        chapterService.deleteChapter(userId, chapterId);
        redirectAttributes.addFlashAttribute("successMessage", "챕터가 성공적으로 삭제되었습니다.");
        return "redirect:/vocabulary/chapters";
    }
}