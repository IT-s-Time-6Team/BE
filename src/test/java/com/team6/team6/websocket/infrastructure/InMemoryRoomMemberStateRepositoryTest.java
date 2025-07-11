package com.team6.team6.websocket.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

class InMemoryRoomMemberStateRepositoryTest {

    private InMemoryRoomMemberStateRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRoomMemberStateRepository();
    }

    @Test
    void 사용자가_방에_있는지_확인한다() {
        // given
        String roomKey = "room1";
        String nickname = "user1";

        // when
        boolean beforeRegister = repository.isUserInRoom(roomKey, nickname);
        repository.registerUserInRoom(roomKey, nickname);
        boolean afterRegister = repository.isUserInRoom(roomKey, nickname);

        // then
        assertSoftly(softly -> {
            softly.assertThat(beforeRegister).isFalse();
            softly.assertThat(afterRegister).isTrue();
        });
    }

    @Test
    void 사용자를_방에_등록하면_첫_연결로_설정된다() {
        // given
        String roomKey = "room1";
        String nickname = "user1";

        // when
        repository.registerUserInRoom(roomKey, nickname);

        // then
        assertSoftly(softly -> {
            softly.assertThat(repository.isUserInRoom(roomKey, nickname)).isTrue();
            softly.assertThat(repository.isUserOnline(roomKey, nickname)).isTrue();
            softly.assertThat(repository.isFirstConnection(roomKey, nickname)).isTrue();
        });
    }

    @Test
    void 첫_연결_여부를_확인한다() {
        // given
        String roomKey = "room1";
        String nickname = "user1";

        // when & then
        assertSoftly(softly -> {
            // 등록 전에는 첫 연결
            softly.assertThat(repository.isFirstConnection(roomKey, nickname)).isTrue();

            // 등록 후에도 첫 연결
            repository.registerUserInRoom(roomKey, nickname);
            softly.assertThat(repository.isFirstConnection(roomKey, nickname)).isTrue();

            // 첫 연결을 false로 설정
            repository.setFirstConnection(roomKey, nickname, false);
            softly.assertThat(repository.isFirstConnection(roomKey, nickname)).isFalse();

            // 다시 true로 설정
            repository.setFirstConnection(roomKey, nickname, true);
            softly.assertThat(repository.isFirstConnection(roomKey, nickname)).isTrue();
        });
    }

    @Test
    void 사용자를_완전히_제거한다() {
        // given
        String roomKey = "room1";
        String nickname = "user1";
        repository.registerUserInRoom(roomKey, nickname);
        repository.setFirstConnection(roomKey, nickname, false);

        // when
        repository.removeUser(roomKey, nickname);

        // then
        assertSoftly(softly -> {
            softly.assertThat(repository.isUserInRoom(roomKey, nickname)).isFalse();
            softly.assertThat(repository.isFirstConnection(roomKey, nickname)).isTrue(); // 제거 후 다시 첫 연결
        });
    }

    @Test
    void 특정_방의_전체_사용자_목록을_조회한다() {
        // given
        String roomKey = "room1";
        String nickname1 = "user1";
        String nickname2 = "user2";

        // when
        repository.registerUserInRoom(roomKey, nickname1);
        repository.registerUserInRoom(roomKey, nickname2);
        Map<String, Boolean> members = repository.getRoomMembers(roomKey);

        // then
        assertSoftly(softly -> {
            softly.assertThat(members).hasSize(2);
            softly.assertThat(members).containsKeys(nickname1, nickname2);
            softly.assertThat(members.get(nickname1)).isTrue();
            softly.assertThat(members.get(nickname2)).isTrue();
        });
    }

    @Test
    void 존재하지_않는_방의_사용자_목록은_빈_맵을_반환한다() {
        // given
        String nonExistingRoom = "nonExistingRoom";

        // when
        Map<String, Boolean> members = repository.getRoomMembers(nonExistingRoom);

        // then
        assertSoftly(softly -> {
            softly.assertThat(members).isNotNull();
            softly.assertThat(members).isEmpty();
        });
    }

    @Test
    void 사용자의_온라인_상태를_확인한다() {
        // given
        String roomKey = "room1";
        String nickname = "user1";

        // when
        repository.registerUserInRoom(roomKey, nickname);
        boolean isOnline = repository.isUserOnline(roomKey, nickname);

        // then
        assertSoftly(softly -> {
            softly.assertThat(isOnline).isTrue();
        });
    }

    @Test
    void 존재하지_않는_사용자의_온라인_상태는_false를_반환한다() {
        // given
        String roomKey = "room1";
        String nickname = "nonExistingUser";

        // when
        boolean isOnline = repository.isUserOnline(roomKey, nickname);

        // then
        assertSoftly(softly -> {
            softly.assertThat(isOnline).isFalse();
        });
    }

    @Test
    void 사용자를_온라인_상태로_설정한다() {
        // given
        String roomKey = "room1";
        String nickname = "user1";
        repository.registerUserInRoom(roomKey, nickname);
        repository.setUserOffline(roomKey, nickname);

        // when
        repository.setUserOnline(roomKey, nickname);

        // then
        assertSoftly(softly -> {
            softly.assertThat(repository.isUserOnline(roomKey, nickname)).isTrue();
        });
    }

    @Test
    void 사용자를_오프라인_상태로_설정한다() {
        // given
        String roomKey = "room1";
        String nickname = "user1";
        repository.registerUserInRoom(roomKey, nickname);

        // when
        repository.setUserOffline(roomKey, nickname);

        // then
        assertSoftly(softly -> {
            softly.assertThat(repository.isUserInRoom(roomKey, nickname)).isTrue();
            softly.assertThat(repository.isUserOnline(roomKey, nickname)).isFalse();
        });
    }

    @Test
    void 특정_방의_온라인_사용자_수를_조회한다() {
        // given
        String roomKey = "room1";
        repository.registerUserInRoom(roomKey, "user1");
        repository.registerUserInRoom(roomKey, "user2");
        repository.registerUserInRoom(roomKey, "user3");
        repository.setUserOffline(roomKey, "user3");

        // when
        int onlineCount = repository.getOnlineUserCount(roomKey);

        // then
        assertSoftly(softly -> {
            softly.assertThat(onlineCount).isEqualTo(2);
        });
    }

    @Test
    void 존재하지_않는_방의_온라인_사용자_수는_0을_반환한다() {
        // given
        String nonExistingRoom = "nonExistingRoom";

        // when
        int onlineCount = repository.getOnlineUserCount(nonExistingRoom);

        // then
        assertSoftly(softly -> {
            softly.assertThat(onlineCount).isZero();
        });
    }
} 