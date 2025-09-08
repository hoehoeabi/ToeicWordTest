package com.example.toeicwordtest.global.config;

import com.example.toeicwordtest.domain.user.dto.UserDto;
import com.example.toeicwordtest.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {

        if (userService.findByUsername("testuser").isEmpty()) {

            UserDto testUserDto = new UserDto();
            testUserDto.setUsername("testuser");
            testUserDto.setNickname("테스트유저");
            testUserDto.setPassword("1234");
            testUserDto.setPasswordCheck("1234");

            userService.signUp(testUserDto);

            System.out.println("====== 테스트용 사용자 'testuser'가 생성되었습니다. ======");
        }
    }
}
