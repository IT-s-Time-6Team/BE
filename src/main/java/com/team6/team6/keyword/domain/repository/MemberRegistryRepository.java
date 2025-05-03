package com.team6.team6.keyword.domain.repository;

import java.util.Map;

public interface MemberRegistryRepository {
    /**
     * 사용자가 방에 있는지 확인
     * @param roomKey 방 식별자
     * @param nickname 사용자 닉네임
     * @return 사용자가 방에 있는지 여부
     */
    boolean isUserInRoom(String roomKey, String nickname);

    /**
     * 사용자를 방에 등록
     * @param roomKey 방 식별자
     * @param nickname 사용자 닉네임
     */
    void registerUserInRoom(String roomKey, String nickname);

    /**
     * 특정 방의 전체 사용자 목록 조회
     * @param roomKey 방 식별자
     * @return 사용자 맵 (닉네임 -> 상태)
     */
    Map<String, Boolean> getRoomMembers(String roomKey);
}