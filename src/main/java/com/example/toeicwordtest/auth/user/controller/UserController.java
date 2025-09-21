package com.example.toeicwordtest.auth.user.controller;

import com.example.toeicwordtest.auth.user.dto.UserDto;
import com.example.toeicwordtest.auth.user.entity.User;
import com.example.toeicwordtest.auth.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("user", new UserDto());
        return "users/signupform";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute("user") UserDto userDto,Model model, RedirectAttributes redirectAttributes) {
        List<String> errors = new ArrayList<>();

        if (!userDto.getPassword().equals(userDto.getPasswordCheck())) {
            errors.add("비밀번호가 일치하지 않습니다.");
        }

        if (errors.isEmpty()) {
            try {
                userService.signUp(userDto);
            } catch (IllegalArgumentException e) {
                errors.add(e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            log.error("회원가입 오류: {}", errors);
            model.addAttribute("errors", errors);
            return "users/signupform";
        }

        redirectAttributes.addFlashAttribute("success", "성공적으로 회원가입 됐습니다.");
        return "redirect:/auth/users/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "users/loginform";
    }

    @GetMapping("/home")
    public String index(Model model, Authentication authentication) {

        String username = authentication.getName();
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isPresent()) {
            model.addAttribute("user", userOptional.get());
            return "users/home";
        }
        return "redirect:/auth/users/login";
    }

}
