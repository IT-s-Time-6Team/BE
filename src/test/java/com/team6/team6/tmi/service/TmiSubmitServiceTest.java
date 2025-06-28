package com.team6.team6.tmi.service;

import com.team6.team6.tmi.domain.TmiMessagePublisher;
import com.team6.team6.tmi.domain.repository.TmiSessionRepository;
import com.team6.team6.tmi.domain.repository.TmiSubmissionRepository;
import com.team6.team6.tmi.dto.TmiSubmitServiceReq;
import com.team6.team6.tmi.entity.TmiSession;
import com.team6.team6.tmi.entity.TmiSubmission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TmiSubmitServiceTest {

    @InjectMocks
    private TmiSubmitService tmiSubmitService;

    @Mock
    private TmiSessionService tmiSessionService;

    @Mock
    private TmiSubmissionRepository tmiSubmissionRepository;

    @Mock
    private TmiMessagePublisher tmiMessagePublisher;

    @Mock
    private TmiHintService tmiHintService;

    @Test
    void TMI_제출_테스트() {
        // given
        TmiSubmitServiceReq request = createTmiRequest();

        TmiSession session = TmiSession.createInitialSession(1L, 4);

        given(tmiSessionService.findTmiSession(1L))
                .willReturn(session);
        given(tmiSubmissionRepository.existsByRoomIdAndMemberId(1L, 1L))
                .willReturn(false);

        // when
        tmiSubmitService.submitTmi(request);

        // then
        verify(tmiSubmissionRepository).save(any(TmiSubmission.class));
        verify(tmiMessagePublisher).notifyTmiCollectionProgress(eq("test-room"), anyInt());
    }

    @Test
    void TMI_재제출시_예외_테스트() {
        // given
        TmiSubmitServiceReq request = createTmiRequest();

        TmiSession session = TmiSession.createInitialSession(1L, 4);

        given(tmiSessionService.findTmiSession(1L))
                .willReturn(session);
        given(tmiSubmissionRepository.existsByRoomIdAndMemberId(1L, 1L))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> tmiSubmitService.submitTmi(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 TMI를 제출했습니다");
    }

    @Test
    void 모든_TMI_수집_테스트() {
        // given
        TmiSubmitServiceReq request = createTmiRequest();

        TmiSession session = TmiSession.createInitialSession(1L, 1); // 총 1명

        given(tmiSessionService.findTmiSession(1L))
                .willReturn(session);
        given(tmiSubmissionRepository.existsByRoomIdAndMemberId(1L, 1L))
                .willReturn(false);

        // when
        tmiSubmitService.submitTmi(request);

        // then
        verify(tmiMessagePublisher).notifyTmiCollectionCompleted("test-room");
        verify(tmiMessagePublisher, never()).notifyTmiCollectionProgress(anyString(), anyInt());
        assertThat(session.isAllTmiCollected()).isTrue();
    }

    private TmiSubmitServiceReq createTmiRequest() {
        return new TmiSubmitServiceReq(
                "저는 고양이를 키워요", "test-room", 1L, 1L, "test-member"
        );
    }
}
