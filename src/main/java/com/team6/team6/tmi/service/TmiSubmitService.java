package com.team6.team6.tmi.service;

import com.team6.team6.tmi.domain.TmiMessagePublisher;
import com.team6.team6.tmi.domain.repository.TmiSessionRepository;
import com.team6.team6.tmi.domain.repository.TmiSubmissionRepository;
import com.team6.team6.tmi.dto.TmiSubmitServiceReq;
import com.team6.team6.tmi.entity.TmiGameStep;
import com.team6.team6.tmi.entity.TmiSession;
import com.team6.team6.tmi.entity.TmiSubmission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmiSubmitService {

    private final TmiSessionRepository tmiSessionRepository;
    private final TmiSubmissionRepository tmiSubmissionRepository;
    private final TmiMessagePublisher tmiMessagePublisher;

    @Transactional
    public void createTmiGameSession(Long roomId, int totalMembers) {
        TmiSession session = TmiSession.createInitialSession(roomId, totalMembers);
        tmiSessionRepository.save(session);
        log.info("TMI 게임 세션 생성: roomId={}, totalMembers={}", roomId, totalMembers);
    }

    @Transactional
    public void submitTmi(TmiSubmitServiceReq req) {
        TmiSession session = findTmiSessionWithLock(req.roomId());

        validateTmiCollectionPhase(session);
        validateDuplicateSubmission(req.roomId(), req.memberId());

        saveTmiSubmission(req);
        updateSessionAndNotify(session, req.roomKey());

        log.info("TMI 제출 완료: roomId={}, memberId={}", req.roomId(), req.memberId());
    }

    private TmiSession findTmiSessionWithLock(Long roomId) {
        return tmiSessionRepository.findByRoomIdWithLock(roomId)
                .orElseThrow(() -> new IllegalStateException("TMI 게임 세션을 찾을 수 없습니다: " + roomId));
    }

    private void validateTmiCollectionPhase(TmiSession session) {
        if (session.getCurrentStep() != TmiGameStep.COLLECTING_TMI) {
            throw new IllegalStateException("TMI 수집 단계가 아닙니다");
        }
    }

    private void validateDuplicateSubmission(Long roomId, Long memberId) {
        if (tmiSubmissionRepository.existsByRoomIdAndMemberId(roomId, memberId)) {
            throw new IllegalStateException("이미 TMI를 제출했습니다");
        }
    }

    private void saveTmiSubmission(TmiSubmitServiceReq req) {
        TmiSubmission submission = req.toEntity();
        tmiSubmissionRepository.save(submission);
    }

    private void updateSessionAndNotify(TmiSession session, String roomKey) {
        session.incrementSubmittedTmiCount();

        int progress = session.calculateCollectionProgress();
        boolean isCompleted = session.isAllTmiCollected();

        log.debug("TMI 수집 진행률: progress={}, isCompleted={}", progress, isCompleted);

        publishProgressMessage(roomKey, progress, isCompleted);
    }

    private void publishProgressMessage(String roomKey, int progress, boolean isCompleted) {
        if (isCompleted) {
            log.debug("모든 TMI 수집 완료. 투표 단계 준비: roomKey={}", roomKey);
            tmiMessagePublisher.publishTmiCollectionCompleted(roomKey);
        } else {
            tmiMessagePublisher.publishTmiCollectionProgress(roomKey, progress);
        }
    }
}
