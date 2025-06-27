package com.team6.team6.tmi.domain.repository;

import com.team6.team6.tmi.entity.TmiVote;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class TmiVoteRepositoryTest {

    @Autowired
    private TmiVoteRepository tmiVoteRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("같은 투표자가 같은 라운드에 중복 투표했는지 확인할 수 있다")
    void existsByRoomIdAndVoterNameAndVotingRound() {
        // given
        TmiVote vote = TmiVote.create(1L, "voter1", "member1", 1L, 0);
        entityManager.persistAndFlush(vote);

        // when & then
        assertThat(tmiVoteRepository.existsByRoomIdAndVoterNameAndVotingRound(1L, "voter1", 0))
                .isTrue();
        assertThat(tmiVoteRepository.existsByRoomIdAndVoterNameAndVotingRound(1L, "voter1", 1))
                .isFalse();
        assertThat(tmiVoteRepository.existsByRoomIdAndVoterNameAndVotingRound(1L, "voter2", 0))
                .isFalse();
    }

    @Test
    @DisplayName("특정 방과 라운드의 모든 투표를 조회할 수 있다")
    void findByRoomIdAndVotingRound() {
        // given
        TmiVote vote1 = TmiVote.create(1L, "voter1", "member1", 1L, 0);
        TmiVote vote2 = TmiVote.create(1L, "voter2", "member2", 1L, 0);
        TmiVote vote3 = TmiVote.create(1L, "voter3", "member1", 1L, 1);
        TmiVote vote4 = TmiVote.create(2L, "voter4", "member1", 1L, 0);

        entityManager.persistAndFlush(vote1);
        entityManager.persistAndFlush(vote2);
        entityManager.persistAndFlush(vote3);
        entityManager.persistAndFlush(vote4);

        // when
        List<TmiVote> votes = tmiVoteRepository.findByRoomIdAndVotingRound(1L, 0);

        // then
        assertThat(votes).hasSize(2);
        assertThat(votes).extracting(TmiVote::getVoterName)
                .containsExactlyInAnyOrder("voter1", "voter2");
    }
}
