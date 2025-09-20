package com.example.toeicwordtest.domain.user.service;

import com.example.toeicwordtest.auth.user.dto.UserDto;
import com.example.toeicwordtest.auth.user.entity.User;
import com.example.toeicwordtest.auth.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // 테스트 후 롤백을 위해 꼭 필요합니다!
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("정상적인 정보로 회원가입이 성공해야 한다.")
    void signUp_Success() {
        // given
        UserDto userDto = new UserDto();
        userDto.setUsername("testuser");
        userDto.setNickname("testnick");
        userDto.setPassword("password123");
        userDto.setPasswordCheck("password123");

        // when
        userService.signUp(userDto);

        // then
        User foundUser = userService.findByUsername("testuser").orElseThrow();

        assertNotNull(foundUser); // 사용자가 null이 아니어야 함
        assertEquals("testnick", foundUser.getNickname()); // 닉네임이 일치해야 함
        assertTrue(foundUser.getPassword().startsWith("$2a$")); // 비밀번호가 암호화되었는지 확인
    }

    @Test
    @DisplayName("중복된 아이디로 회원가입을 시도하면 예외가 발생해야 한다.")
    void signUp_Fail_DuplicateUsername() {
        // given
        UserDto existingUserDto = new UserDto();
        existingUserDto.setUsername("existinguser");
        existingUserDto.setNickname("existingnick");
        existingUserDto.setPassword("password123");
        existingUserDto.setPasswordCheck("password123");
        userService.signUp(existingUserDto);

        // when
        UserDto newUserDto = new UserDto();
        newUserDto.setUsername("existinguser"); // 중복된 아이디
        newUserDto.setNickname("newnick");
        newUserDto.setPassword("password123");
        newUserDto.setPasswordCheck("password123");

        // then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.signUp(newUserDto);
        });
    }
}