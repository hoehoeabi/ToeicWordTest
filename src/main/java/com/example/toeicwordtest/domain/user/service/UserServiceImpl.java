package com.example.toeicwordtest.domain.user.service;

import com.example.toeicwordtest.domain.role.entity.Role;
import com.example.toeicwordtest.domain.role.repository.RoleRepository;
import com.example.toeicwordtest.domain.user.dto.UserDto;
import com.example.toeicwordtest.domain.user.entity.User;
import com.example.toeicwordtest.domain.user.repository.UserRepository;
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

        Role role = roleRepository.findByName(userDto.getUsername().equals("admin") ? "ADMIN" : "USER");

        User user = new User();
        user.setRoles(Collections.singleton(role));
        user.setUsername(userDto.getUsername());
        user.setUsername(userDto.getUsername());
        user.setNickname(userDto.getNickname());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

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
