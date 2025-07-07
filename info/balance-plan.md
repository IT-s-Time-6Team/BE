# Balance 모드 개발 계획서

## 개요

Balance 모드는 참여자들이 두 가지 선택지 중 하나를 선택하고, 다수의 선택에 따라 점수를 획득하는 게임 모드입니다. TMI 모드의 구조를 기반으로 하되, 밸런스 게임 특성에 맞게 설계됩니다.

## 게임 메커니즘

Balance 모드는 **라운드 기반 게임**으로, 각 라운드는 4단계로 구성됩니다:

1. **문제 공개 (QUESTION_REVEAL)**: 밸런스 문제 공개 (30초 고정)
2. **토론 시간 (DISCUSSION)**: 자유 토론 (5분, 방장 건너뛰기 가능)
3. **투표 시간 (VOTING)**: A/B 선택지 투표
4. **결과 확인 (RESULT_VIEW)**: 투표 결과 및 점수 변동 확인

모든 문제 완료 후 **최종 결과 (COMPLETED)** 단계로 전환

## 핵심 엔티티 설계

### 1. BalanceQuestion (기존 테이블)

```java
@Entity
@Table(name = "BALANCE_QUESTION")
public class BalanceQuestion extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_a")
    private String questionA;

    @Column(name = "question_b")
    private String questionB;
}
```

### 2. BalanceSession (새로 생성)

```java
@Entity
public class BalanceSession extends BaseEntity {
    private Long roomId;                        // 방 식별자
    private BalanceGameStep currentStep;        // 현재 게임 단계
    private Integer totalMembers;               // 총 참여자 수
    private Integer totalQuestions;             // 총 문제 수
    private Integer currentQuestionIndex;       // 현재 문제 인덱스 (0부터 시작)
    private Integer currentVotedMemberCount;    // 현재 라운드 투표 완료자 수
    private LocalDateTime closedAt;             // 게임 종료 시간
}
```

### 3. BalanceGameQuestion (새로 생성)

```java
@Entity
public class BalanceGameQuestion extends BaseEntity {
    private Long roomId;                // 방 식별자
    private Long balanceQuestionId;     // BALANCE_QUESTION 테이블 참조
    private String questionA;           // A 선택지 (복사해서 저장)
    private String questionB;           // B 선택지 (복사해서 저장)
    private Integer displayOrder;       // 문제 출제 순서 (0부터 시작)
}
```

### 4. BalanceVote (새로 생성)

```java
@Entity
public class BalanceVote extends BaseEntity {
    private Long roomId;                    // 방 식별자
    private String voterName;               // 투표자 닉네임
    private Long memberId;                  // 투표자 ID
    private BalanceChoice selectedChoice;   // A 또는 B (enum)
    private Integer votingRound;            // 투표 라운드 (문제 순서와 동일)
    private Long balanceGameQuestionId;     // 해당 문제 ID
}
```

### 5. BalanceMemberScore (새로 생성)

```java
@Entity
public class BalanceMemberScore extends BaseEntity {
    private Long roomId;            // 방 식별자
    private Long memberId;          // 참여자 ID
    private String memberName;      // 참여자 닉네임
    private Integer currentScore;   // 현재 점수
    private Integer totalCorrect;   // 총 맞춘 횟수
    private Integer totalWrong;     // 총 틀린 횟수
}
```

## Enum 정의

### BalanceGameStep

```java
public enum BalanceGameStep {
    QUESTION_REVEAL,    // 문제 공개 단계
    DISCUSSION,         // 토론 단계
    VOTING,            // 투표 단계
    RESULT_VIEW,       // 결과 확인 단계
    COMPLETED          // 게임 완료
}
```

### BalanceChoice

```java
public enum BalanceChoice {
    A("A"),
    B("B");

    private final String value;
}
```

## 방 생성 로직 수정

### RoomCreateRequest 수정

```java
public record RoomCreateRequest(
    // 기존 필드들...

    @Min(value = 1, message = "밸런스 문제 개수는 최소 1개 이상이어야 합니다")
    @Max(value = 7, message = "밸런스 문제 개수는 최대 7개입니다")
    Integer balanceQuestionCount    // Balance 모드 전용
) {

    private Integer setBalanceQuestionCount() {
        if (gameMode == GameMode.BALANCE) {
            return balanceQuestionCount == null ? 5 : balanceQuestionCount;
        }
        return null;
    }
}
```

### GameMode 추가

```java
@Getter
@RequiredArgsConstructor
public enum GameMode {
    NORMAL("일반"),
    TMI("TMI"),
    BALANCE("밸런스");    // 추가

    private final String description;
}
```

## 서비스 레이어 설계

### 1. BalanceSessionService

- **책임**: 게임 세션 생성, 상태 조회, 락 관리
- **핵심 메서드**:
  - `createBalanceGameSession(Long roomId, int totalMembers, int questionCount)`
  - `getSessionStatus(Long roomId, String memberName)`
  - `findSessionByRoomIdWithLock(Long roomId)`

### 2. BalanceQuestionService

- **책임**: 문제 선택 및 관리
- **핵심 메서드**:
  - `selectRandomQuestionsForRoom(Long roomId, int questionCount)`
  - `getCurrentQuestion(Long roomId)`
  - `getQuestionByDisplayOrder(Long roomId, int order)`

### 3. BalanceRevealService

- **책임**: 문제 공개 단계 관리
- **핵심 메서드**:
  - `startQuestionReveal(String roomKey, Long roomId)`
  - 30초 타이머 관리 (강제, 건너뛰기 불가)
  - 타이머 종료 시 자동으로 토론 단계 시작

### 4. BalanceDiscussionService

- **책임**: 토론 단계 관리
- **핵심 메서드**:
  - `startDiscussionTime(String roomKey, Long roomId)`
  - `skipDiscussion(String roomKey, Long roomId, String leaderName)` // 방장만 가능
  - 5분 타이머 관리 (건너뛰기 가능)

### 5. BalanceVoteService

- **책임**: 투표 처리 및 점수 계산
- **핵심 메서드**:
  - `startVotingPhase(String roomKey, Long roomId)`
  - `submitVote(BalanceVoteServiceReq req)`
  - `calculateScores(Long roomId, int currentRound)`

### 6. BalanceResultService

- **책임**: 결과 조회 및 게임 완료 처리
- **핵심 메서드**:
  - `getRoundResult(Long roomId, int round)`
  - `getFinalResults(Long roomId)`
  - `proceedToNextRound(String roomKey, Long roomId)`

## 게임 플로우 상세

### 1. 방 생성 시 초기화

```java
// RoomService.java
if (request.gameMode() == GameMode.BALANCE) {
    balanceSessionService.createBalanceGameSession(
        savedRoom.getId(),
        request.maxMember(),
        request.balanceQuestionCount()
    );
    balanceQuestionService.selectRandomQuestionsForRoom(
        savedRoom.getId(),
        request.balanceQuestionCount()
    );
}
```

### 2. 문제 공개 단계 (30초 고정)

- 현재 라운드 문제 공개
- 30초 타이머 시작 (건너뛰기 불가)
- WebSocket으로 문제 내용 및 남은 시간 브로드캐스트
- 타이머 종료 시 자동으로 토론 단계 시작

### 3. 토론 단계 (5분, 건너뛰기 가능)

- 5분 타이머 시작
- 방장만 건너뛰기 버튼 활성화
- 채팅을 통한 자유 토론
- 시간 종료 또는 건너뛰기 시 투표 단계 시작

### 4. 투표 단계

- A/B 선택지 투표
- 실시간 투표율 표시
- 모든 참여자 투표 완료 시 자동으로 결과 확인 단계

### 5. 결과 확인 단계

- 투표 결과 (A/B 비율)
- 점수 계산 및 변동량 표시
- 현재 순위 표시
- 다음 라운드 여부 확인

## 점수 계산 시스템

### 점수 계산 로직

```java
public class BalanceScoreCalculator {
    public void calculateRoundScores(Long roomId, int round) {
        // 1. 해당 라운드 모든 투표 조회
        // 2. A/B 선택 비율 계산
        // 3. 다수 선택지 결정
        // 4. 점수 업데이트
        //    - 다수 선택: +1점
        //    - 소수 선택: -1점
        //    - 동률: 점수 변동 없음
    }
}
```

## API 엔드포인트 설계

### Balance 관련 API

- `GET /balance/rooms/{roomKey}/status` - 게임 상태 조회
- `GET /balance/rooms/{roomKey}/question` - 현재 문제 조회
- `POST /balance/rooms/{roomKey}/discussion/skip` - 토론 건너뛰기 (방장만)
- `POST /balance/rooms/{roomKey}/votes` - 투표 제출
- `GET /balance/rooms/{roomKey}/result/{round}` - 라운드 결과 조회
- `GET /balance/rooms/{roomKey}/final-result` - 최종 결과 조회

## WebSocket 메시지 타입

### Balance 전용 메시지

```java
// BalanceChatMessage.java
// 문제 공개 단계 (30초)
public static final String TYPE_BALANCE_QUESTION_STARTED = "BALANCE_QUESTION_STARTED";
public static final String TYPE_BALANCE_QUESTION_TIME_REMAINING = "BALANCE_QUESTION_TIME_REMAINING";
public static final String TYPE_BALANCE_QUESTION_ENDED = "BALANCE_QUESTION_ENDED";

// 토론 단계 (5분)
public static final String TYPE_BALANCE_DISCUSSION_STARTED = "BALANCE_DISCUSSION_STARTED";
public static final String TYPE_BALANCE_DISCUSSION_TIME_REMAINING = "BALANCE_DISCUSSION_TIME_REMAINING";
public static final String TYPE_BALANCE_DISCUSSION_ENDED = "BALANCE_DISCUSSION_ENDED";

// 기타
public static final String TYPE_BALANCE_DISCUSSION_SKIPPED = "BALANCE_DISCUSSION_SKIPPED";
public static final String TYPE_BALANCE_VOTING_STARTED = "BALANCE_VOTING_STARTED";
public static final String TYPE_BALANCE_VOTING_PROGRESS = "BALANCE_VOTING_PROGRESS";
public static final String TYPE_BALANCE_ROUND_COMPLETED = "BALANCE_ROUND_COMPLETED";
public static final String TYPE_BALANCE_GAME_COMPLETED = "BALANCE_GAME_COMPLETED";
```

## 일급 컬렉션 설계

### BalanceVotes

```java
public class BalanceVotes {
    private final List<BalanceVote> votes;

    // A/B 선택 비율 계산
    public BalanceVoteResult calculateVoteResult();

    // 다수 선택지 반환
    public BalanceChoice getMajorityChoice();

    // 동률 여부 확인
    public boolean isTie();
}
```

### BalanceGameQuestions

```java
public class BalanceGameQuestions {
    private final List<BalanceGameQuestion> questions;

    // 순서별 문제 조회
    public BalanceGameQuestion getQuestionByOrder(int order);

    // 총 문제 수
    public int getTotalCount();

    // 남은 문제 수
    public int getRemainingCount(int currentIndex);
}
```

## DTO 설계

### 요청 DTO

```java
public record BalanceVoteRequest(
    BalanceChoice selectedChoice    // A 또는 B
) {}

public record BalanceDiscussionSkipRequest() {}
```

### 응답 DTO

```java
public record BalanceQuestionResponse(
    String questionA,
    String questionB,
    int currentRound,
    int totalRounds
) {}

public record BalanceRoundResultResponse(
    BalanceChoice myChoice,
    int choiceACount,
    int choiceBCount,
    double choiceAPercentage,
    double choiceBPercentage,
    BalanceChoice majorityChoice,
    boolean isTie,
    int scoreChange,
    int currentScore,
    int currentRank,
    int currentRound,
    List<BalanceMemberScoreInfo> allMemberScores
) {}

public record BalanceFinalResultResponse(
    String memberName,
    int finalScore,
    int finalRank,
    List<String> mostBalancedQuestions,     // 가장 비율이 비슷했던 질문들
    List<String> mostUnanimousQuestions     // 가장 만장일치에 가까웠던 질문들
) {}
```

## 동시성 제어

### 비관적 락 적용

```java
// BalanceSessionRepository.java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT bs FROM BalanceSession bs WHERE bs.roomId = :roomId")
Optional<BalanceSession> findByRoomIdWithLock(@Param("roomId") Long roomId);
```

### 중복 투표 방지

```java
// BalanceVoteRepository.java
boolean existsByRoomIdAndMemberIdAndVotingRound(Long roomId, Long memberId, Integer round);
```

## 타이머 시스템

### 문제 공개 타이머 (30초, 강제)

```java
// BalanceRevealService.java
TimerConfig config = TimerConfig.of(timerKey, roomKey, roomId, 30, "BALANCE_REVEAL");
gameTimerService.startTimer(config);
```

### 토론 타이머 (5분, 건너뛰기 가능)

```java
// BalanceDiscussionService.java
TimerConfig config = TimerConfig.of(timerKey, roomKey, roomId, 300, "BALANCE_DISCUSSION");
gameTimerService.startTimer(config);

// 건너뛰기 기능
public void skipDiscussion(String roomKey, Long roomId, String memberName) {
    // 방장 권한 확인
    // 타이머 강제 종료
    // 투표 단계 시작
}
```

## 개발 우선순위

### Phase 1: 핵심 구조 구축

1. 엔티티 및 Repository 생성
2. 기본 서비스 레이어 구현
3. 방 생성 로직 수정

### Phase 2: 게임 플로우 구현

1. 문제 공개 → 토론 → 투표 → 결과 플로우
2. 타이머 시스템 연동
3. WebSocket 메시지 구현

### Phase 3: 점수 시스템 및 결과

1. 점수 계산 로직 구현
2. 순위 시스템 구현
3. 최종 결과 분석 기능

### Phase 4: 고도화

1. 건너뛰기 기능 구현
2. 실시간 진행률 표시
3. 테스트 코드 작성

## 예상 이슈 및 고려사항

### 1. 타이머 동기화

- 클라이언트-서버 간 시간 동기화 문제
- 네트워크 지연으로 인한 타이머 오차

### 2. 건너뛰기 권한

- 방장 권한 확인 로직
- 방장 이탈 시 권한 이양 방식

### 3. 점수 계산 정확성

- 동시 투표 시 점수 계산 일관성
- 라운드별 점수 누적 정확성

### 4. 확장성

- 추후 다른 밸런스 게임 모드 추가 가능성
- 문제 카테고리화 및 난이도 조절

이 계획서를 바탕으로 TMI 모드의 검증된 구조를 활용하여 Balance 모드를 안정적으로 구현할 수 있을 것입니다.
