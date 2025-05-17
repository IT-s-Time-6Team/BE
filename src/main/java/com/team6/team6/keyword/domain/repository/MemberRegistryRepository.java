package com.team6.team6.keyword.domain.repository;

import java.util.Map;

/**
 * 방에 있는 사용자 정보를 관리하는 레포지토리 인터페이스
 */
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
     * @return 사용자 맵 (닉네임 -> 온라인 상태)
     */
    Map<String, Boolean> getRoomMembers(String roomKey);

    /**
     * 사용자의 온라인 상태를 확인
     *
     * @param roomKey  방 식별자
     * @param nickname 사용자 닉네임
     * @return 사용자가 온라인인지 여부
     */
    boolean isUserOnline(String roomKey, String nickname);

    /**
     * 사용자를 온라인 상태로 설정
     *
     * @param roomKey  방 식별자
     * @param nickname 사용자 닉네임
     */
    void setUserOnline(String roomKey, String nickname);

    /**
     * 사용자를 오프라인 상태로 설정
     *
     * @param roomKey  방 식별자
     * @param nickname 사용자 닉네임
     */
    void setUserOffline(String roomKey, String nickname);

    /**
     * 특정 방의 온라인 사용자 수 조회
     *
     * @param roomKey 방 식별자
     * @return 온라인 사용자 수
     */
    int getOnlineUserCount(String roomKey);

}
