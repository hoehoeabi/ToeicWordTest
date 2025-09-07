package com.example.toeicwordtest.global.security;

import com.example.toeicwordtest.domain.role.entity.Role;
import com.example.toeicwordtest.domain.user.entity.User;
import com.example.toeicwordtest.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        log.info("로그인 시도 이메일: {}", username);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            log.info("로그인 시도 실패. 해당 이메일 디비에 없음: {}", username);
            throw new UsernameNotFoundException("사용자가 존재하지 않습니다: " + username);
        }

        User foundUser = userOptional.get();
        Set<Role> roles = foundUser.getRoles();

        return new CustomUserDetails(
                foundUser.getUsername(),
                foundUser.getPassword(),
                foundUser.getNickname(),
                roles
        );
    }
}
