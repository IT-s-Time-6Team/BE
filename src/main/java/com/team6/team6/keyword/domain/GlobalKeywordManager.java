package com.team6.team6.keyword.domain;

import com.team6.team6.keyword.domain.repository.GlobalKeywordRepository;
import com.team6.team6.keyword.domain.repository.KeywordGroupRepository;
import com.team6.team6.keyword.dto.AnalysisResult;
import com.team6.team6.keyword.entity.GlobalKeyword;
import com.team6.team6.keyword.entity.KeywordGroup;
import com.team6.team6.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GlobalKeywordManager {

    private final KeywordPreprocessor keywordPreprocessor;
    private final GlobalKeywordRepository globalKeywordRepository;
    private final KeywordGroupRepository keywordGroupRepository;
    private final QuestionService questionService;

    // 키워드 정규화: 분석 결과의 키워드를 전처리하여 일관된 형식으로 저장
    @Transactional
    public void normalizeKeyword(AnalysisResult result, String newKeyword) {
        // 모든 키워드 전처리
        List<String> preprocessedKeywords = result.variations()
                .stream().map(keywordPreprocessor::preprocess)
                .toList();
        // 새 키워드 전처리
        String preprocessedNewKeyword = keywordPreprocessor.preprocess(newKeyword);
        log.debug("전처리된 키워드 목록: {}, 새 키워드: {}", preprocessedKeywords, preprocessedNewKeyword);

        // newKeyword와 동일한 키워드가 입력된 적 있다면, 메서드 종료
        if (alreadyProcessed(preprocessedKeywords, preprocessedNewKeyword)) {
            log.info("이미 처리된 중복 키워드: {}", preprocessedNewKeyword);
            return;
        }

        // 새 키워드가 global keyword에 존재하는지 확인
        Optional<GlobalKeyword> existingNewKeyword = globalKeywordRepository.findByKeyword(preprocessedNewKeyword);
        if (existingNewKeyword.isPresent()) {
            // global keyword에 존재할 때 처리
            handleExistingKeyword(preprocessedKeywords, existingNewKeyword.get());
        } else {
            // global keyword에 존재하지 않을 때 처리
            handleNewKeyword(preprocessedKeywords, preprocessedNewKeyword);
        }
        log.info("키워드 정규화 완료: {}", newKeyword);
    }

    private void handleNewKeyword(List<String> preprocessedKeywords, String preprocessedNewKeyword) {
        // 그룹화된 키워드 내 global keyword 조회
        log.debug("새 키워드 처리 시작: {}", preprocessedNewKeyword);
        List<GlobalKeyword> others = globalKeywordRepository.findByKeywordIn(preprocessedKeywords);
        log.debug("연관 키워드 수: {}", others.size());

        if (!others.isEmpty()) {
            // 그룹화된 키워드 내 global keyword가 존재하면 해당 그룹에 새 키워드 추가
            KeywordGroup existingGroup = others.get(0).getKeywordGroup();
            GlobalKeyword newGlobalKeyword = GlobalKeyword.create(preprocessedNewKeyword, existingGroup);
            globalKeywordRepository.save(newGlobalKeyword);
        } else {
            // 아무 그룹도 없으면 새 그룹 생성
            KeywordGroup newGroup = KeywordGroup.create(preprocessedNewKeyword);
            keywordGroupRepository.save(newGroup);
            GlobalKeyword newGlobalKeyword = GlobalKeyword.create(preprocessedNewKeyword, newGroup);
            globalKeywordRepository.save(newGlobalKeyword);
            log.info("새 키워드 '{}'와 그룹 '{}'이 생성되었습니다..", preprocessedNewKeyword, newGroup.getId());
            generateQuestionsAfterCommit(preprocessedNewKeyword, newGroup);
        }
    }

    private void generateQuestionsAfterCommit(String preprocessedNewKeyword, KeywordGroup newGroup) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                questionService.generateQuestions(preprocessedNewKeyword, newGroup);
            }
        });
    }

    private void handleExistingKeyword(List<String> preprocessedKeywords, GlobalKeyword globalKeyword) {
        KeywordGroup newKeywordGroup = globalKeyword.getKeywordGroup();
        List<GlobalKeyword> others = globalKeywordRepository.findByKeywordIn(preprocessedKeywords);

        // 다른 키워드 그룹과 충돌 확인
        Set<KeywordGroup> otherGroups = others.stream()
                .map(GlobalKeyword::getKeywordGroup)
                .filter(group -> !group.equals(newKeywordGroup))
                .collect(Collectors.toSet());

        if (otherGroups.isEmpty()) {
            return;
        }

        // 충돌이 발생한 경우, 그룹 병합
        int updateKeywordCounts = globalKeywordRepository.bulkUpdateKeywordGroups(newKeywordGroup, otherGroups);
        log.info("키워드 그룹 병합 완료: {} 그룹의 키워드 {} 개가 {} 그룹으로 이동되었습니다.", otherGroups, updateKeywordCounts, newKeywordGroup.getId());
    }

    private boolean alreadyProcessed(List<String> preprocessedKeywords, String preprocessedNewKeyword) {
        long matchCount = preprocessedKeywords.stream()
                .filter(keyword -> keyword.equals(preprocessedNewKeyword))
                .count();
        return matchCount > 1;
    }
}
