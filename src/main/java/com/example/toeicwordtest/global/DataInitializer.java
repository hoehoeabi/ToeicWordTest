package com.example.toeicwordtest.global;

import com.example.toeicwordtest.auth.role.entity.Role;
import com.example.toeicwordtest.auth.role.repository.RoleRepository;
import com.example.toeicwordtest.auth.user.dto.UserDto;
import com.example.toeicwordtest.auth.user.entity.User;
import com.example.toeicwordtest.auth.user.service.UserService;
import com.example.toeicwordtest.vocabulary.domain.chapter.entity.Chapter;
import com.example.toeicwordtest.vocabulary.domain.chapter.repository.ChapterRepository;
import com.example.toeicwordtest.vocabulary.domain.word.entity.Word;
import com.example.toeicwordtest.vocabulary.domain.wrongnote.entity.WrongNoteEntry;
import com.example.toeicwordtest.vocabulary.domain.wrongnote.repository.WrongNoteEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final ChapterRepository chapterRepository;
    private final WrongNoteEntryRepository wrongNoteEntryRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // 1. 역할(Role) 생성
        if (roleRepository.findByRoleName("USER").isEmpty()) {
            roleRepository.save(new Role("USER"));
        }
        if (roleRepository.findByRoleName("ADMIN").isEmpty()) {
            roleRepository.save(new Role("ADMIN"));
        }

        // 2. 테스트용 사용자 생성 및 조회
        if (userService.findByUsername("testuser").isEmpty()) {
            UserDto testUserDto = new UserDto();
            testUserDto.setUsername("testuser");
            testUserDto.setNickname("테스트유저");
            testUserDto.setPassword("1234");
            testUserDto.setPasswordCheck("1234");
            userService.signUp(testUserDto);
            System.out.println("====== 테스트용 사용자 'testuser'가 생성되었습니다. ======");
        }
        User testUser = userService.findByUsername("testuser").orElseThrow();


        // 3. 챕터 및 단어 추가 (챕터 1이 없으면 모두 생성)
        if (chapterRepository.findByUserAndChapterNumber(testUser, 1).isEmpty()) {

            // --- 챕터 1 및 단어 생성 ---
            Chapter chapter1 = Chapter.builder()
                    .chapterNumber(1)
                    .title("기초 영단어")
                    .user(testUser) // ★ 빌더에서 바로 유저 설정
                    .build();

            List<Word> chapter1Words = Arrays.asList(
                    Word.builder().spelling("apple").meaning("사과").build(),
                    Word.builder().spelling("banana").meaning("바나나").build(),
                    Word.builder().spelling("cherry").meaning("체리").build()
            );
            // ★ 연관관계 편의 메소드를 사용하여 단어 추가
            chapter1Words.forEach(chapter1::addWord);
            chapterRepository.save(chapter1); // ★ Chapter만 저장해도 Word가 함께 저장됨 (Cascade)
            System.out.println("====== Chapter 1 ('기초 영단어') 및 단어 3개가 생성되었습니다. ======");


            // --- 챕터 2 및 단어 생성 ---
            Chapter chapter2 = Chapter.builder()
                    .chapterNumber(2)
                    .title("동사 & 형용사")
                    .user(testUser)
                    .build();

            List<Word> chapter2Words = Arrays.asList(
                    Word.builder().spelling("run").meaning("달리다").build(),
                    Word.builder().spelling("eat").meaning("먹다").build(),
                    Word.builder().spelling("beautiful").meaning("아름다운").build()
            );
            chapter2Words.forEach(chapter2::addWord);

            chapterRepository.save(chapter2);
            System.out.println("====== Chapter 2 ('동사 & 형용사') 및 단어 3개가 생성되었습니다. ======");

            // --- 4. 오답 기록 (WrongNoteEntry) 생성 ---
            // testUser가 chapter1의 'apple'과 chapter2의 'eat'을 틀렸다고 가정
            List<Word> wrongWords = Arrays.asList(chapter1Words.get(0), chapter2Words.get(1)); // apple, eat

            for (Word word : wrongWords) {
                WrongNoteEntry entry = WrongNoteEntry.builder()
                        .user(testUser)
                        .word(word)
                        .build();
                wrongNoteEntryRepository.save(entry);
            }

            // --- 5. 생성된 데이터 로그 출력 ---
            List<WrongNoteEntry> savedWrongEntries = wrongNoteEntryRepository.findByUser(testUser);
            // ★★★ 에러가 발생했던 String.format() 수정 ★★★
            String wrongEntryWordsString = savedWrongEntries.stream()
                    .map(entry -> String.format("단어: %s (%s)",
                            entry.getWord().getSpelling(),
                            entry.getWord().getMeaning()))
                    .collect(Collectors.joining(", "));
            System.out.println("====== '" + testUser.getNickname() + "'의 오답 기록 2건이 추가되었습니다. [" + wrongEntryWordsString + "] ======");
        }
    }
}