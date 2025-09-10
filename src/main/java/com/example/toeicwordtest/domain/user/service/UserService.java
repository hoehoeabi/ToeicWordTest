package com.example.toeicwordtest.domain.user.service;

import com.example.toeicwordtest.domain.user.dto.UserDto;
import com.example.toeicwordtest.domain.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserService {

    void signUp(UserDto userDto);

    Optional<User> findByUsername(String username);

    void deleteUser(String email);
}
