package com.example.toeicwordtest.auth.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor // 기본 생성자(매개변수가 없는 생성자)를 생성
@AllArgsConstructor // 클래스의 모든 필드를 매개변수로 받는 생성자를 생성
@Builder
public class UserDto {

    private String username;
    private String password;
    private String passwordCheck;
    private String nickname;

}


