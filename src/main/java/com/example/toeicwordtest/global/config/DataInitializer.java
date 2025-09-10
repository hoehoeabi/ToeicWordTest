package com.example.toeicwordtest.global.config;

import com.example.toeicwordtest.domain.chapter.entity.Chapter;
import com.example.toeicwordtest.domain.chapter.repository.ChapterRepository;
import com.example.toeicwordtest.domain.user.dto.UserDto;
import com.example.toeicwordtest.domain.user.entity.User;
import com.example.toeicwordtest.domain.user.service.UserService;
import com.example.toeicwordtest.domain.word.entity.Word;
import com.example.toeicwordtest.domain.word.repository.WordRepository;
import com.example.toeicwordtest.domain.wrongnote.entity.WrongNote;
import com.example.toeicwordtest.domain.wrongnote.repository.WrongNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final ChapterRepository chapterRepository;
    private final WordRepository wordRepository;
    private final WrongNoteRepository wrongNoteRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        User testUser = null;

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

            // Chapter 1 단어
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

            // Chapter 2 단어
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

            // 오답노트 생성 및 단어 추가
            WrongNote wrongNote = WrongNote.builder()
                    .user(testUser)
                    .words(new HashSet<>())
                    .build();

            wrongNote.getWords().add(chapter1Words.get(0));
            wrongNote.getWords().add(chapter1Words.get(1));
            wrongNoteRepository.save(wrongNote);

            String wrongNoteWordsString = wrongNote.getWords().stream()
                    .map(word -> String.format("%s(챕터%d): %s", word.getSpelling(), word.getChapter().getChapterNumber(), word.getMeaning()))
                    .collect(Collectors.joining(", "));
            System.out.println("====== '" + testUser.getNickname() + "'의 오답노트에 단어 2개가 추가되었습니다. [" + wrongNoteWordsString + "] ======");
        }
    }
}