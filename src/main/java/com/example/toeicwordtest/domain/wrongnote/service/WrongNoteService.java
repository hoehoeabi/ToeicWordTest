package com.example.toeicwordtest.domain.wrongnote.service;

import com.example.toeicwordtest.domain.user.entity.User;
import com.example.toeicwordtest.domain.user.repository.UserRepository;
import com.example.toeicwordtest.domain.wrongnote.entity.WrongNoteEntry;
import com.example.toeicwordtest.domain.wrongnote.repository.WrongNoteEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WrongNoteService {
    private final WrongNoteEntryRepository wrongNoteEntryRepository;
    private final UserRepository userRepository;

    // (N+1 유발 로직)
    public void printWrongWords(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  User에 해당하는 모든 WrongNoteEntry를 조회 (★ 쿼리 1번 발생)
        List<WrongNoteEntry> entries = wrongNoteEntryRepository.findByUser(user);

        System.out.println("--- 쿼리 실행 지점 ---");

        // 각 WrongNoteEntry에 대해 Word 정보를 가져오기 위해 루프 실행
        for (WrongNoteEntry entry : entries) {
            // entry.getWord() 까지는 괜찮지만, .getSpelling()을 호출하는 순간
            // LAZY 로딩으로 인해 Word 프록시 객체가 실제 데이터를 채우기 위해 쿼리를 날림.
            //  여기서 N개의 쿼리가 추가로 발생!
            System.out.println("틀린 단어: " + entry.getWord().getSpelling());
        }
    }

    // (fetch를 통해 N+1 해결 로직)
    public void printWrongWords_Optimized(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch Join이 적용된 메소드를 호출 ( 쿼리 1번으로 모든 것을 끝냄)
        List<WrongNoteEntry> entries = wrongNoteEntryRepository.findByUserFetchJoinWord(user);

        System.out.println("--- 쿼리 실행 지점 ---");

        //  루프를 실행해도 추가 쿼리가 발생하지 않음
        for (WrongNoteEntry entry : entries) {
            // 이미 Word 객체가 채워져 있으므로, 추가 쿼리가 발생하지 않음
            System.out.println("틀린 단어: " + entry.getWord().getSpelling());
        }
    }
}
