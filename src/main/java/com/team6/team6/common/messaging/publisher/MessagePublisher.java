package com.team6.team6.common.messaging.publisher;

/**
 * 메시지 발행을 위한 인터페이스
 * <p>
 * 다양한 프로토콜(STOMP, SSE, Long Polling 등)을 통해 클라이언트에게
 * 실시간 메시지를 발행하기 위한 추상화 계층
 * 이 인터페이스를 구현하는 클래스는 특정 프로토콜의 메시지 발행 메커니즘을 제공
 * </p>
 */
public interface MessagePublisher {

    /**
     * 키워드 분석 결과를 발행
     *
     * @param roomId 메시지를 발행할 대상 룸의 ID
     * @param results 발행할 키워드 분석 결과 객체
     */
    void publishKeywordAnalysisResult(Long roomId, Object results);
}