package com.example.toeicwordtest.vocabulary.service;

import com.example.toeicwordtest.auth.user.entity.User;
import com.example.toeicwordtest.auth.user.repository.UserRepository;
import com.example.toeicwordtest.vocabulary.chapter.entity.Chapter;
import com.example.toeicwordtest.vocabulary.chapter.repository.ChapterRepository;
import com.example.toeicwordtest.vocabulary.dto.ChapterDto;
import com.example.toeicwordtest.vocabulary.dto.WordDto;
import com.example.toeicwordtest.vocabulary.mapper.VocabularyMapper;
import com.example.toeicwordtest.vocabulary.word.entity.Word;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final VocabularyMapper vocabularyMapper;


    public ChapterDto createChapter(Long userId, ChapterDto chapterDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // 챕터 번호 중복 확인
        if (chapterRepository.findByUserAndChapterNumber(user, chapterDto.getChapterNumber()).isPresent()) {
            throw new IllegalArgumentException("Chapter number " + chapterDto.getChapterNumber() + " already exists.");
        }

        Chapter chapter = Chapter.builder()
                .chapterNumber(chapterDto.getChapterNumber())
                .title(chapterDto.getTitle())
                .build();
        chapter.setUser(user); // 연관관계 설정

        // 초기 단어 추가
        if (chapterDto.getWords() != null && !chapterDto.getWords().isEmpty()) {
            chapterDto.getWords().forEach(wordDto -> {
                Word newWord = Word.builder()
                        .spelling(wordDto.getSpelling())
                        .meaning(wordDto.getMeaning())
                        .build();
                chapter.addWord(newWord);
            });
        }
        Chapter savedChapter = chapterRepository.save(chapter);
        return vocabularyMapper.toChapterDto(savedChapter);
    }

    // --- 수정(Update) 로직 ---
    public ChapterDto updateChapter(Long userId, Long chapterId, ChapterDto chapterDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Chapter chapter = chapterRepository.findByIdAndUser(chapterId, user)
                .orElseThrow(() -> new SecurityException("Chapter not found or you don't have permission."));

        // 수정 시, 본인을 제외하고 챕터 번호가 중복되는지 확인
        chapterRepository.findByUserAndChapterNumber(user, chapterDto.getChapterNumber())
                .ifPresent(existingChapter -> {
                    if (!existingChapter.getId().equals(chapterId)) {
                        throw new IllegalArgumentException("Chapter number " + chapterDto.getChapterNumber() + " already exists.");
                    }
                });

        chapter.updateChapterDetails(chapterDto.getChapterNumber(), chapterDto.getTitle());
        updateChapterWords(chapter, chapterDto.getWords());

        Chapter updatedChapter = chapterRepository.save(chapter);
        return vocabularyMapper.toChapterDto(updatedChapter);
    }

    /**
     * 챕터에 속한 단어들을 업데이트합니다. (CRUD 포함)
     * 기존 단어와 DTO로 넘어온 단어들을 비교하여 추가, 수정, 삭제를 처리합니다.
     */
    private void updateChapterWords(Chapter chapter, List<WordDto> newWordDtos) {
        // null 방어
        if (newWordDtos == null) {
            newWordDtos = List.of();
        }

        // 현재 챕터에 연결된 기존 단어들을 Map으로 만듭니다 (ID로 빠르게 찾기 위함)
        // Set으로 단어 ID만 추출 (삭제 로직을 위해)
        Set<Long> existingWordIdsInChapter = chapter.getWords().stream()
                .map(Word::getId)
                .collect(Collectors.toSet());

        // DTO로 넘어온 단어들의 ID (수정 및 추가 로직을 위해)
        Set<Long> newWordIdsInDto = newWordDtos.stream()
                .filter(dto -> dto.getId() != null)
                .map(WordDto::getId)
                .collect(Collectors.toSet());

        // 1. 삭제할 단어 처리: 기존 단어 중 새 단어 목록에 없는 것들을 제거
        // chapter.getWords()는 FetchType.LAZY일 수 있으므로 주의.
        // 현재 로직에서는 이미 트랜잭션 내에서 chapter가 관리되므로 문제없음.
        // CopyOnWriteArrayList 등을 사용하면 concurrent modification exception을 피할 수 있지만
        // 여기서는 stream으로 필터링 후 toList()로 새로운 리스트 생성하여 안전하게 처리
        List<Word> wordsToDelete = chapter.getWords().stream()
                .filter(word -> !newWordIdsInDto.contains(word.getId()))
                .toList(); // ConcurrentModificationException 방지를 위해 새 리스트 생성

        wordsToDelete.forEach(chapter::removeWord); // Chapter에서 Word 관계 끊기 (orphanRemoval = true로 DB에서도 삭제)


        // 2. 추가/수정할 단어 처리
        for (WordDto wordDto : newWordDtos) {
            if (wordDto.getId() == null) {
                // 새 단어 추가
                Word newWord = Word.builder()
                        .spelling(wordDto.getSpelling())
                        .meaning(wordDto.getMeaning())
                        .build();
                chapter.addWord(newWord);
            } else {
            // 기존 단어 수정
                // 챕터의 words 컬렉션에서 해당 ID의 단어를 찾아 업데이트
                chapter.getWords().stream()
                        .filter(word -> word.getId().equals(wordDto.getId()))
                        .findFirst()
                        .ifPresentOrElse(
                                word -> word.updateWordDetails(wordDto.getSpelling(), wordDto.getMeaning()), // 엔티티의 비즈니스 메서드 호출
                                () -> {
                                    // DTO에 ID가 있는데, 해당 ID의 단어가 현재 Chapter의 Word 컬렉션에 없는 경우
                                    // 이는 다른 챕터에 속한 단어를 잘못 전달했거나, 이미 삭제된 단어를 수정하려는 시도일 수 있음
                                    // 비정상적인 요청.
                                    throw new IllegalArgumentException("Word with ID " + wordDto.getId() + " not found within chapter " + chapter.getId() + ". It might belong to another chapter or has been deleted.");
                                }
                        );
            }
        }
    }


    /**
     * 특정 챕터를 삭제합니다.
     * @param userId 챕터를 소유한 유저의 ID
     * @param chapterId 삭제할 챕터의 ID
     */
    public void deleteChapter(Long userId, Long chapterId) {
// 해당 유저의 챕터인지 확인하며 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Chapter chapter = chapterRepository.findByIdAndUser(chapterId, user)
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found with id: " + chapterId + " for user: " + userId));

        chapterRepository.delete(chapter); // cascade = ALL, orphanRemoval = true로 단어도 함께 삭제
    }

    /**
     * 특정 챕터의 정보를 조회합니다. (단어 목록 포함)
     * @param userId 챕터를 소유한 유저의 ID
     * @param chapterId 조회할 챕터의 ID
     * @return 챕터 정보 DTO
     */
    @Transactional(readOnly = true)
    public ChapterDto getChapter(Long userId, Long chapterId) {
        // Fetch Join을 사용하여 챕터와 단어를 한 번에 가져오는 쿼리 (N+1 방지)
        // ChapterRepositoryCustomImpl의 findChapterWithWordsById 메서드를 활용
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Chapter chapter = chapterRepository.findChapterWithWordsById(chapterId)
                .filter(ch -> ch.getUser().getId().equals(user.getId())) // 현재 유저의 챕터인지 추가 확인
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found with id: " + chapterId + " for user: " + userId));

        return vocabularyMapper.toChapterDto(chapter);
    }

    /**
     * 특정 유저의 모든 챕터 목록을 조회합니다.
     * @param userId 유저의 ID
     * @return 챕터 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<ChapterDto> getAllChaptersForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Fetch Join을 사용하여 챕터와 단어를 한 번에 가져오도록 Repository에 메서드 추가 가능
        // 여기서는 일단 기본 로딩 방식으로 처리
        List<Chapter> chapters = chapterRepository.findByUserOrderByChapterNumberAsc(user);

        // N+1 문제 발생 가능성: 각 Chapter의 words가 Lazy 로딩이므로
        // DTO 변환 시 words에 접근할 때마다 추가 쿼리 발생할 수 있음.
        // 해결: findByUserOrderByChapterNumberAsc 쿼리에 @EntityGraph 또는 Fetch Join 적용
        return chapters.stream()
                .map(vocabularyMapper::toChapterDto)
                .collect(Collectors.toList());
    }

    // 여러 챕터 ID로부터 모든 단어 목록을 가져옴
    @Transactional(readOnly = true)
    public List<Word> getWordsFromChapters(Long userId, List<Long> chapterIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Chapter> chapters = chapterRepository.findByIdInAndUser(chapterIds, user);

        // 조회된 챕터 수가 요청된 ID 수와 다르면, 권한이 없거나 존재하지 않는 챕터 ID가 포함된 것
        if (chapters.size() != chapterIds.size()) {
            throw new SecurityException("You do not have permission for some of the selected chapters.");
        }

        return chapters.stream()
                .flatMap(chapter -> chapter.getWords().stream())
                .collect(Collectors.toList());
    }
}