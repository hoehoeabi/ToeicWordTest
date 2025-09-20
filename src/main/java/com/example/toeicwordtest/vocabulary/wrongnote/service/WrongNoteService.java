package com.example.toeicwordtest.vocabulary.wrongnote.service;

import com.example.toeicwordtest.auth.user.entity.User;
import com.example.toeicwordtest.auth.user.repository.UserRepository;
import com.example.toeicwordtest.vocabulary.dto.ChapterDto;
import com.example.toeicwordtest.vocabulary.dto.WordDto;
import com.example.toeicwordtest.vocabulary.mapper.VocabularyMapper;
import com.example.toeicwordtest.vocabulary.word.entity.Word;
import com.example.toeicwordtest.vocabulary.wrongnote.entity.WrongNoteEntry;
import com.example.toeicwordtest.vocabulary.wrongnote.repository.WrongNoteEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WrongNoteService {
    private final WrongNoteEntryRepository wrongNoteEntryRepository;
    private final UserRepository userRepository;
    private final VocabularyMapper vocabularyMapper;

    public void addWrongNoteEntry(User user, Word word) {
        WrongNoteEntry entry = WrongNoteEntry.builder()
                .user(user)
                .word(word)
                .build();

        if (user != null) { // user가 null이 아닐 때만 관계 설정 시도
            user.addWrongNoteEntry(entry);
        }

        if (word != null) { // word가 null이 아닐 때만 관계 설정 시도
            word.addWrongNoteEntry(entry);
        }

        wrongNoteEntryRepository.save(entry);
    }

    // ★ 틀린 단어 목록에서 맞춘 단어 제거
    public void removeWrongNoteEntry(User user, Word word) {
        wrongNoteEntryRepository.findByUserAndWord(user, word)
                .ifPresent(wrongNoteEntryRepository::delete);
    }

    // ★ 오답노트 시험을 위해 단어 목록을 가져오는 기능
    @Transactional(readOnly = true)
    public List<Word> getWordsFromWrongNote(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return wrongNoteEntryRepository.findByUserFetchJoinWord(user).stream()
                .map(WrongNoteEntry::getWord)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public void printWrongWords(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<WrongNoteEntry> entries = wrongNoteEntryRepository.findByUser(user);
        System.out.println("--- 쿼리 실행 지점 ---");
        for (WrongNoteEntry entry : entries) {
            System.out.println("틀린 단어: " + entry.getWord().getSpelling());
        }
    }

    @Transactional(readOnly = true)
    public void printWrongWords_Optimized(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<WrongNoteEntry> entries = wrongNoteEntryRepository.findByUserFetchJoinWord(user);
        System.out.println("--- 쿼리 실행 지점 ---");
        for (WrongNoteEntry entry : entries) {
            System.out.println("틀린 단어: " + entry.getWord().getSpelling());
        }
    }

    // ★ 추가된 메소드: 특정 유저의 틀린 단어 개수 조회
    @Transactional(readOnly = true)
    public int getWrongWordsCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return (int) wrongNoteEntryRepository.countByUser(user);
    }

    // ★ 추가된 메소드: 틀린 단어 목록을 상세 보기용 ChapterDto로 변환하여 반환
    @Transactional(readOnly = true)
    public ChapterDto getWrongNoteChapter(Long userId) {
        List<Word> wrongWords = getWordsFromWrongNote(userId);
        List<WordDto> wrongWordDtos = wrongWords.stream()
                .map(vocabularyMapper::toWordDto)
                .collect(Collectors.toList());

        ChapterDto dto = ChapterDto.createWrongNoteChapter(wrongWordDtos.size());
        dto.setWords(wrongWordDtos); // 실제 단어 목록을 설정
        return dto;
    }

}