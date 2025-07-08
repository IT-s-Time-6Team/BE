package com.team6.team6.tmi.domain.repository;

import com.team6.team6.member.entity.CharacterType;
import com.team6.team6.tmi.entity.TmiVote;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DataJpaTest
class TmiVoteRepositoryTest {

    @Autowired
    private TmiVoteRepository tmiVoteRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void 투표자_중복_투표_테스트() {
        // given
        TmiVote vote = TmiVote.create(1L, "voter1", 1L, CharacterType.CHICK, "member1", 1L, CharacterType.BEAR, 1L, 0);
        entityManager.persistAndFlush(vote);

        // when & then
        assertSoftly(softly -> {
            softly.assertThat(tmiVoteRepository.existsByRoomIdAndVoterNameAndVotingRound(1L, "voter1", 0))
                    .isTrue();
            softly.assertThat(tmiVoteRepository.existsByRoomIdAndVoterNameAndVotingRound(1L, "voter1", 1))
                    .isFalse();
            softly.assertThat(tmiVoteRepository.existsByRoomIdAndVoterNameAndVotingRound(1L, "voter2", 0))
                    .isFalse();
        });
    }

    @Test
    void 방의_모든_라운드_투표_조회_테스트() {
        // given
        TmiVote vote1 = TmiVote.create(1L, "voter1", 1L, CharacterType.CHICK, "member1", 1L, CharacterType.BEAR, 1L, 0);
        TmiVote vote2 = TmiVote.create(1L, "voter2", 2L, CharacterType.RABBIT, "member2", 1L, CharacterType.BEAR, 1L, 0);
        TmiVote vote3 = TmiVote.create(1L, "voter3", 3L, CharacterType.PANDA, "member1", 1L, CharacterType.BEAR, 1L, 0);
        TmiVote vote4 = TmiVote.create(2L, "voter4", 4L, CharacterType.PIG, "member1", 1L, CharacterType.BEAR, 1L, 0);

        entityManager.persistAndFlush(vote1);
        entityManager.persistAndFlush(vote2);
        entityManager.persistAndFlush(vote3);
        entityManager.persistAndFlush(vote4);

        // when
        List<TmiVote> votes = tmiVoteRepository.findByRoomIdAndVotingRound(1L, 0);

        // then
        assertSoftly(softly -> {
            softly.assertThat(votes).hasSize(3);
            softly.assertThat(votes).extracting(TmiVote::getVoterName)
                    .containsExactlyInAnyOrder("voter1", "voter2", "voter3");
        });
    }
}
