package com.example.toeicwordtest.auth.user.service;

import com.example.toeicwordtest.auth.role.entity.Role;
import com.example.toeicwordtest.auth.role.repository.RoleRepository;
import com.example.toeicwordtest.auth.user.dto.UserDto;
import com.example.toeicwordtest.auth.user.entity.User;
import com.example.toeicwordtest.auth.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void signUp(UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByNickname(userDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        String roleName = userDto.getUsername().equals("admin") ? "ADMIN" : "USER";
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException(roleName + " 역할을 찾을 수 없습니다."));

        User user = User.builder()
                .username(userDto.getUsername())
                .nickname(userDto.getNickname())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .roles(Collections.singleton(role))
                .build();


        userRepository.save(user);
    }


    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void deleteUser(String email) {
        Optional<User> emailOptional = userRepository.findByUsername(email);
        if (emailOptional.isPresent()) {
            userRepository.delete(emailOptional.get());
        } else {
            throw new RuntimeException("삭제할 사용자가 존재하지 않습니다.");
        }
    }

}
