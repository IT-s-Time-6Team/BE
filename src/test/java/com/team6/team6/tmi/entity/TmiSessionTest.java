package com.team6.team6.tmi.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class TmiSessionTest {

    @Test
    void TMI_세션_생성_테스트() {
        // given
        Long roomId = 1L;
        int totalMembers = 4;

        // when
        TmiSession session = TmiSession.createInitialSession(roomId, totalMembers);

        // then
        assertSoftly(softly -> {
            softly.assertThat(session.getRoomId()).isEqualTo(roomId);
            softly.assertThat(session.getCurrentStep()).isEqualTo(TmiGameStep.COLLECTING_TMI);
            softly.assertThat(session.getTotalMembers()).isEqualTo(totalMembers);
            softly.assertThat(session.getSubmittedTmiCount()).isZero();
            softly.assertThat(session.getCurrentVotingTmiIndex()).isZero();
            softly.assertThat(session.getCurrentVotedMemberCount()).isZero();
        });
    }

    @Test
    void TMI_개수_증가_테스트() {
        // given
        TmiSession session = TmiSession.createInitialSession(1L, 4);
        int initialCount = session.getSubmittedTmiCount();

        // when
        session.incrementSubmittedTmiCount();

        // then
        assertThat(session.getSubmittedTmiCount()).isEqualTo(initialCount + 1);
    }

    @Test
    void TMI_수집_진행률_계산_테스트() {
        // given
        TmiSession session = TmiSession.createInitialSession(1L, 4);
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount(); // 2개 제출

        // when
        int progress = session.calculateCollectionProgress();

        // then
        assertThat(progress).isEqualTo(50);
    }

    @Test
    void 모든_TMI_수집_완료_테스트() {
        // given
        TmiSession session = TmiSession.createInitialSession(1L, 2);
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount(); // 2개 모두 제출

        // when
        boolean isCompleted = session.isAllTmiCollected();

        // then
        assertThat(isCompleted).isTrue();
    }

    @Test
    void TMI_수집_미완료_테스트() {
        // given
        TmiSession session = TmiSession.createInitialSession(1L, 4);
        session.incrementSubmittedTmiCount(); // 1개만 제출

        // when
        boolean isCompleted = session.isAllTmiCollected();

        // then
        assertThat(isCompleted).isFalse();
    }
}
