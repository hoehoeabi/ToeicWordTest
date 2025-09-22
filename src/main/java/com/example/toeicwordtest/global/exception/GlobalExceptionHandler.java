package com.example.toeicwordtest.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
        log.error("IllegalArgumentException: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        // 폼 입력 실패 등 현재 페이지에 메시지를 보여줘야 할 경우
        // 여기서는 일반적인 에러 페이지로 포워딩합니다.
        return "error"; // templates/error.html
    }

    @ExceptionHandler(SecurityException.class)
    public String handleSecurityException(SecurityException ex, RedirectAttributes redirectAttributes) {
        log.error("SecurityException: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        // 권한 문제 등 작업 실패 후 이전 목록 페이지 등으로 리다이렉트할 경우
        return "redirect:/vocabulary/chapters";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        log.error("Unhandled Exception: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage", "요청 처리 중 오류가 발생했습니다. 관리자에게 문의하세요.");
        return "error";
    }
}