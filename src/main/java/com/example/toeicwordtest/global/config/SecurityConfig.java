package com.example.toeicwordtest.global.config;

import com.example.toeicwordtest.global.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(authorize -> authorize
                                .requestMatchers("/auth/users/signup", "/auth/users/login","/h2-console/**").permitAll()
                                .anyRequest().authenticated()
                        )

                .formLogin(form -> form
                                .loginPage("/auth/users/login") // 로그인 페이지의 경로
                                .loginProcessingUrl("/auth/users/login") // 로그인 폼이 제출되는 URL
                                .defaultSuccessUrl("/auth/users/home")
                                .permitAll()
                        )
                .logout(logout -> logout
                        .logoutUrl("/auth/users/logout") // 로그아웃을 처리할 URL
                        .logoutSuccessUrl("/auth/users/login") // 로그아웃 성공 후 이동할 페이지
                        .invalidateHttpSession(true)      // HTTP 세션을 무효화 >> HttpSession 폐기
                        .deleteCookies("JSESSIONID")      // JSESSIONID 쿠키 삭제
                        .permitAll()
                )
                .sessionManagement(sessionManagement -> sessionManagement
                        // 최대 허용 세션 수 1
                        .maximumSessions(1)
                        // 동시 로그인을 차단
                        .maxSessionsPreventsLogin(true)
                )
                .userDetailsService(customUserDetailsService);
                //.csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
