package com.example.toeicwordtest.global.config;

import com.example.toeicwordtest.domain.chapter.entity.Chapter;
import com.example.toeicwordtest.domain.chapter.repository.ChapterRepository;
import com.example.toeicwordtest.domain.role.entity.Role;
import com.example.toeicwordtest.domain.role.repository.RoleRepository;
import com.example.toeicwordtest.domain.user.dto.UserDto;
import com.example.toeicwordtest.domain.user.entity.User;
import com.example.toeicwordtest.domain.user.service.UserService;
import com.example.toeicwordtest.domain.word.entity.Word;
import com.example.toeicwordtest.domain.word.repository.WordRepository;
import com.example.toeicwordtest.domain.wrongnote.entity.WrongNoteEntry;
import com.example.toeicwordtest.domain.wrongnote.repository.WrongNoteEntryRepository;
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
    private final WordRepository wordRepository;
    private final WrongNoteEntryRepository wrongNoteEntryRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (roleRepository.findByName("USER").isEmpty()) {
            roleRepository.save(new Role("USER"));
            System.out.println("====== 'USER' 역할이 생성되었습니다. ======");
        }
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            roleRepository.save(new Role("ADMIN"));
            System.out.println("====== 'ADMIN' 역할이 생성되었습니다. ======");
        }

        User testUser = null;

        // 테스트용 사용자 생성
        if (userService.findByUsername("testuser").isEmpty()) {
            UserDto testUserDto = new UserDto();
            testUserDto.setUsername("testuser");
            testUserDto.setNickname("테스트유저");
            testUserDto.setPassword("1234");
            testUserDto.setPasswordCheck("1234");
            userService.signUp(testUserDto);
            System.out.println("====== 테스트용 사용자 'testuser'가 생성되었습니다. ======");
        }
        testUser = userService.findByUsername("testuser").orElseThrow();

        // 챕터 추가
        if (chapterRepository.findByChapterNumber(1).isEmpty()) {
            Chapter chapter1 = Chapter.builder()
                    .chapterNumber(1)
                    .user(testUser)
                    .build();
            chapterRepository.save(chapter1);
            System.out.println("====== Chapter 1이 생성되었습니다. ======");

            Chapter chapter2 = Chapter.builder()
                    .chapterNumber(2)
                    .user(testUser)
                    .build();
            chapterRepository.save(chapter2);
            System.out.println("====== Chapter 2가 생성되었습니다. ======");

            // 각 챕터에 단어 추가
            List<Word> chapter1Words = Arrays.asList(
                    Word.builder().spelling("apple").meaning("사과").chapter(chapter1).build(),
                    Word.builder().spelling("banana").meaning("바나나").chapter(chapter1).build(),
                    Word.builder().spelling("cherry").meaning("체리").chapter(chapter1).build()
            );
            wordRepository.saveAll(chapter1Words);
            String chapter1WordsString = chapter1Words.stream()
                    .map(word -> word.getSpelling() + ": " + word.getMeaning())
                    .collect(Collectors.joining(", "));
            System.out.println("====== Chapter 1에 단어 3개가 추가되었습니다. [" + chapter1WordsString + "] ======");

            List<Word> chapter2Words = Arrays.asList(
                    Word.builder().spelling("dog").meaning("개").chapter(chapter2).build(),
                    Word.builder().spelling("elephant").meaning("코끼리").chapter(chapter2).build(),
                    Word.builder().spelling("fox").meaning("여우").chapter(chapter2).build()
            );
            wordRepository.saveAll(chapter2Words);
            String chapter2WordsString = chapter2Words.stream()
                    .map(word -> word.getSpelling() + ": " + word.getMeaning())
                    .collect(Collectors.joining(", "));
            System.out.println("====== Chapter 2에 단어 3개가 추가되었습니다. [" + chapter2WordsString + "] ======");

            // 오답 기록 (WrongNoteEntry) 생성 및 단어 추가
            // testUser가 chapter1Words의 첫 두 단어를 틀렸다는 기록을 생성
            List<Word> wrongWords = Arrays.asList(chapter1Words.get(0), chapter1Words.get(1)); // apple, banana

            for (Word word : wrongWords) {
                // 특정 유저가 특정 단어를 이미 틀린 기록이 있는지 확인 (중복 기록 방지)
                if (wrongNoteEntryRepository.findByUserAndWord(testUser, word).isEmpty()) {
                    WrongNoteEntry entry = WrongNoteEntry.builder()
                            .user(testUser)
                            .word(word)
                            // .wrongDate(LocalDateTime.now()) // @Builder.Default가 처리함
                            .build();
                    wrongNoteEntryRepository.save(entry);
                }
            }

            // 저장된 오답 기록들을 다시 조회해서 출력
            List<WrongNoteEntry> savedWrongEntries = wrongNoteEntryRepository.findByUser(testUser);
            String wrongEntryWordsString = savedWrongEntries.stream()
                    .map(entry -> String.format("%s(챕터%d): %s [틀린 날짜: %s]",
                            entry.getWord().getSpelling(),
                            entry.getWord().getChapter().getChapterNumber(),
                            entry.getWord().getMeaning(),
                            entry.getWrongDate().toLocalDate())) // 날짜만 출력
                    .collect(Collectors.joining(",\n "));
            System.out.println("====== '" + testUser.getNickname() + "'의 오답 기록 2개가 추가되었습니다. ======\n" + wrongEntryWordsString);
        }
    }
}