package com.example.toeicwordtest.auth.user.service;

import com.example.toeicwordtest.auth.user.dto.UserDto;
import com.example.toeicwordtest.auth.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserService {

    void signUp(UserDto userDto);

    Optional<User> findByUsername(String username);

    void deleteUser(String username);
}
