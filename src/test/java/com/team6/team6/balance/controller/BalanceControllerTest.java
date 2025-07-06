package com.team6.team6.balance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.team6.balance.dto.*;
import com.team6.team6.balance.entity.BalanceChoice;
import com.team6.team6.balance.entity.BalanceGameStep;
import com.team6.team6.balance.service.*;
import com.team6.team6.global.security.AuthUtil;
import com.team6.team6.member.entity.CharacterType;
import com.team6.team6.member.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BalanceController.class)
@AutoConfigureRestDocs
@DisplayName("BalanceController 테스트")
class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private BalanceVoteService balanceVoteService;

    @MockitoBean
    private BalanceSessionService balanceSessionService;

    @MockitoBean
    private BalanceDiscussionService balanceDiscussionService;

    @MockitoBean
    private BalanceResultService balanceResultService;

    private UserPrincipal createMockUser() {
        return new UserPrincipal(1L, "testUser", 1L, "ROOM123", CharacterType.RABBIT, "BALANCE");
    }

    @Test
    @DisplayName("게임 상태 조회 API")
    void getGameStatus() throws Exception {
        // given
        String roomKey = "ROOM123";
        UserPrincipal mockUser = createMockUser();
        
        BalanceSessionStatusResponse response = BalanceSessionStatusResponse.builder()
                .currentStep(BalanceGameStep.VOTING)
                .hasUserSubmitted(false)
                .waitingForOthers(false)
                .progress(0)
                .currentRound(1)
                .totalRounds(3)
                .build();

        try (MockedStatic<AuthUtil> authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(AuthUtil::getCurrentUser).thenReturn(mockUser);
            given(balanceSessionService.getSessionStatus(anyLong(), anyString()))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/balance/rooms/{roomKey}/status", roomKey)
                    .header("Authorization", "Bearer valid-token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.data.currentStep").value("VOTING"))
                    .andExpect(jsonPath("$.data.hasUserSubmitted").value(false))
                    .andExpect(jsonPath("$.data.currentRound").value(1))
                    .andExpect(jsonPath("$.data.totalRounds").value(3))
                    .andDo(document("balance-game-status",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName("Authorization").description("Bearer 토큰")
                            ),
                            pathParameters(
                                    parameterWithName("roomKey").description("방 키")
                            ),
                            responseFields(
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description("HTTP 상태"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                    fieldWithPath("data.currentStep").type(JsonFieldType.STRING).description("현재 게임 단계"),
                                    fieldWithPath("data.hasUserSubmitted").type(JsonFieldType.BOOLEAN).description("사용자 투표 제출 여부"),
                                    fieldWithPath("data.waitingForOthers").type(JsonFieldType.BOOLEAN).description("다른 사람들 대기 여부"),
                                    fieldWithPath("data.progress").type(JsonFieldType.NUMBER).description("진행률"),
                                    fieldWithPath("data.currentRound").type(JsonFieldType.NUMBER).description("현재 라운드"),
                                    fieldWithPath("data.totalRounds").type(JsonFieldType.NUMBER).description("총 라운드 수")
                            )
                    ));
        }
    }

    @Test
    @DisplayName("현재 투표 정보 조회 API")
    void getCurrentVotingInfo() throws Exception {
        // given
        String roomKey = "ROOM123";
        UserPrincipal mockUser = createMockUser();
        
        BalanceVotingStartResponse response = BalanceVotingStartResponse.of("치킨", "피자");

        try (MockedStatic<AuthUtil> authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(AuthUtil::getCurrentUser).thenReturn(mockUser);
            given(balanceVoteService.getCurrentVotingInfo(anyLong()))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/balance/rooms/{roomKey}/votes", roomKey)
                    .header("Authorization", "Bearer valid-token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.data.questionA").value("치킨"))
                    .andExpect(jsonPath("$.data.questionB").value("피자"))
                    .andDo(document("balance-voting-info",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName("Authorization").description("Bearer 토큰")
                            ),
                            pathParameters(
                                    parameterWithName("roomKey").description("방 키")
                            ),
                            responseFields(
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description("HTTP 상태"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                    fieldWithPath("data.questionA").type(JsonFieldType.STRING).description("선택지 A"),
                                    fieldWithPath("data.questionB").type(JsonFieldType.STRING).description("선택지 B")
                            )
                    ));
        }
    }

    @Test
    @DisplayName("투표 제출 API")
    void submitVote() throws Exception {
        // given
        String roomKey = "ROOM123";
        UserPrincipal mockUser = createMockUser();
        
        BalanceVoteRequest request = new BalanceVoteRequest(BalanceChoice.A);

        try (MockedStatic<AuthUtil> authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(AuthUtil::getCurrentUser).thenReturn(mockUser);
            doNothing().when(balanceVoteService).submitVote(any(BalanceVoteServiceReq.class));

            // when & then
            mockMvc.perform(post("/balance/rooms/{roomKey}/votes", roomKey)
                    .header("Authorization", "Bearer valid-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andDo(document("balance-submit-vote",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName("Authorization").description("Bearer 토큰")
                            ),
                            pathParameters(
                                    parameterWithName("roomKey").description("방 키")
                            ),
                            requestFields(
                                    fieldWithPath("selectedChoice").type(JsonFieldType.STRING).description("선택한 답변 (A 또는 B)")
                            ),
                            responseFields(
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description("HTTP 상태"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                    fieldWithPath("data").type(JsonFieldType.STRING).description("응답 데이터").optional()
                            )
                    ));
        }
    }

    @Test
    @DisplayName("토론 건너뛰기 API")
    void skipDiscussion() throws Exception {
        // given
        String roomKey = "ROOM123";
        UserPrincipal mockUser = createMockUser();

        try (MockedStatic<AuthUtil> authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(AuthUtil::getCurrentUser).thenReturn(mockUser);
            doNothing().when(balanceDiscussionService).skipDiscussion(anyString(), anyLong(), anyString());

            // when & then
            mockMvc.perform(post("/balance/rooms/{roomKey}/discussion/skip", roomKey)
                    .header("Authorization", "Bearer valid-token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andDo(document("balance-skip-discussion",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName("Authorization").description("Bearer 토큰")
                            ),
                            pathParameters(
                                    parameterWithName("roomKey").description("방 키")
                            ),
                            responseFields(
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description("HTTP 상태"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                    fieldWithPath("data").type(JsonFieldType.STRING).description("응답 데이터").optional()
                            )
                    ));
        }
    }

    @Test
    @DisplayName("최신 투표 결과 조회 API")
    void getLatestVotingResult() throws Exception {
        // given
        String roomKey = "ROOM123";
        UserPrincipal mockUser = createMockUser();
        
        BalanceMemberScoreInfo memberScore = BalanceMemberScoreInfo.builder()
                .memberName("testUser")
                .currentScore(1)
                .scoreChange(1)
                .rank(1)
                .build();

        BalanceRoundResultResponse response = BalanceRoundResultResponse.builder()
                .myChoice(BalanceChoice.A)
                .choiceACount(2)
                .choiceBCount(1)
                .choiceAPercentage(66.67)
                .choiceBPercentage(33.33)
                .majorityChoice(BalanceChoice.A)
                .isTie(false)
                .scoreChange(1)
                .currentScore(1)
                .currentRank(1)
                .currentRound(1)
                .allMemberScores(List.of(memberScore))
                .build();

        try (MockedStatic<AuthUtil> authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(AuthUtil::getCurrentUser).thenReturn(mockUser);
            given(balanceResultService.getLatestVotingResult(anyLong(), anyString()))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/balance/rooms/{roomKey}/votes/result", roomKey)
                    .header("Authorization", "Bearer valid-token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.data.myChoice").value("A"))
                    .andExpect(jsonPath("$.data.choiceACount").value(2))
                    .andExpect(jsonPath("$.data.choiceBCount").value(1))
                    .andExpect(jsonPath("$.data.majorityChoice").value("A"))
                    .andExpect(jsonPath("$.data.isTie").value(false))
                    .andExpect(jsonPath("$.data.scoreChange").value(1))
                    .andExpect(jsonPath("$.data.currentScore").value(1))
                    .andExpect(jsonPath("$.data.currentRank").value(1))
                    .andExpect(jsonPath("$.data.currentRound").value(1))
                    .andDo(document("balance-voting-result",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName("Authorization").description("Bearer 토큰")
                            ),
                            pathParameters(
                                    parameterWithName("roomKey").description("방 키")
                            ),
                            responseFields(
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description("HTTP 상태"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                    fieldWithPath("data.myChoice").type(JsonFieldType.STRING).description("내가 선택한 답변"),
                                    fieldWithPath("data.choiceACount").type(JsonFieldType.NUMBER).description("A 선택 인원 수"),
                                    fieldWithPath("data.choiceBCount").type(JsonFieldType.NUMBER).description("B 선택 인원 수"),
                                    fieldWithPath("data.choiceAPercentage").type(JsonFieldType.NUMBER).description("A 선택 비율"),
                                    fieldWithPath("data.choiceBPercentage").type(JsonFieldType.NUMBER).description("B 선택 비율"),
                                    fieldWithPath("data.majorityChoice").type(JsonFieldType.STRING).description("다수파 선택"),
                                    fieldWithPath("data.isTie").type(JsonFieldType.BOOLEAN).description("동점 여부"),
                                    fieldWithPath("data.scoreChange").type(JsonFieldType.NUMBER).description("점수 변화량"),
                                    fieldWithPath("data.currentScore").type(JsonFieldType.NUMBER).description("현재 점수"),
                                    fieldWithPath("data.currentRank").type(JsonFieldType.NUMBER).description("현재 순위"),
                                    fieldWithPath("data.currentRound").type(JsonFieldType.NUMBER).description("현재 라운드"),
                                    fieldWithPath("data.allMemberScores[]").type(JsonFieldType.ARRAY).description("모든 멤버 점수 정보"),
                                    fieldWithPath("data.allMemberScores[].memberName").type(JsonFieldType.STRING).description("멤버 이름"),
                                    fieldWithPath("data.allMemberScores[].currentScore").type(JsonFieldType.NUMBER).description("현재 점수"),
                                    fieldWithPath("data.allMemberScores[].scoreChange").type(JsonFieldType.NUMBER).description("점수 변화량"),
                                    fieldWithPath("data.allMemberScores[].rank").type(JsonFieldType.NUMBER).description("순위")
                            )
                    ));
        }
    }

    @Test
    @DisplayName("최종 결과 조회 API")
    void getSessionResults() throws Exception {
        // given
        String roomKey = "ROOM123";
        UserPrincipal mockUser = createMockUser();
        
        BalanceQuestionSummary balancedQuestion = BalanceQuestionSummary.of(1, "치킨", "피자");
        BalanceQuestionSummary unanimousQuestion = BalanceQuestionSummary.of(2, "개", "고양이");
        
        BalanceFinalResultResponse response = BalanceFinalResultResponse.builder()
                .memberName("testUser")
                .finalScore(5)
                .finalRank(1)
                .winnerNickname("winner")
                .mostBalancedQuestions(List.of(balancedQuestion))
                .mostUnanimousQuestions(List.of(unanimousQuestion))
                .build();

        try (MockedStatic<AuthUtil> authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(AuthUtil::getCurrentUser).thenReturn(mockUser);
            given(balanceResultService.getSessionResults(anyLong(), anyString()))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/balance/rooms/{roomKey}/results", roomKey)
                    .header("Authorization", "Bearer valid-token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.data.memberName").value("testUser"))
                    .andExpect(jsonPath("$.data.finalScore").value(5))
                    .andExpect(jsonPath("$.data.finalRank").value(1))
                    .andExpect(jsonPath("$.data.winnerNickname").value("winner"))
                    .andExpect(jsonPath("$.data.mostBalancedQuestions").isArray())
                    .andExpect(jsonPath("$.data.mostBalancedQuestions[0].round").value(1))
                    .andExpect(jsonPath("$.data.mostBalancedQuestions[0].questionA").value("치킨"))
                    .andExpect(jsonPath("$.data.mostBalancedQuestions[0].questionB").value("피자"))
                    .andExpect(jsonPath("$.data.mostUnanimousQuestions").isArray())
                    .andExpect(jsonPath("$.data.mostUnanimousQuestions[0].round").value(2))
                    .andExpect(jsonPath("$.data.mostUnanimousQuestions[0].questionA").value("개"))
                    .andExpect(jsonPath("$.data.mostUnanimousQuestions[0].questionB").value("고양이"))
                    .andDo(document("balance-final-results",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName("Authorization").description("Bearer 토큰")
                            ),
                            pathParameters(
                                    parameterWithName("roomKey").description("방 키")
                            ),
                            responseFields(
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description("HTTP 상태"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                    fieldWithPath("data.memberName").type(JsonFieldType.STRING).description("멤버 이름"),
                                    fieldWithPath("data.finalScore").type(JsonFieldType.NUMBER).description("최종 점수"),
                                    fieldWithPath("data.finalRank").type(JsonFieldType.NUMBER).description("최종 순위"),
                                    fieldWithPath("data.winnerNickname").type(JsonFieldType.STRING).description("우승자 닉네임"),
                                    fieldWithPath("data.mostBalancedQuestions[]").type(JsonFieldType.ARRAY).description("가장 균형잡힌 문제들"),
                                    fieldWithPath("data.mostBalancedQuestions[].round").type(JsonFieldType.NUMBER).description("라운드 번호"),
                                    fieldWithPath("data.mostBalancedQuestions[].questionA").type(JsonFieldType.STRING).description("선택지 A"),
                                    fieldWithPath("data.mostBalancedQuestions[].questionB").type(JsonFieldType.STRING).description("선택지 B"),
                                    fieldWithPath("data.mostUnanimousQuestions[]").type(JsonFieldType.ARRAY).description("가장 일치한 문제들"),
                                    fieldWithPath("data.mostUnanimousQuestions[].round").type(JsonFieldType.NUMBER).description("라운드 번호"),
                                    fieldWithPath("data.mostUnanimousQuestions[].questionA").type(JsonFieldType.STRING).description("선택지 A"),
                                    fieldWithPath("data.mostUnanimousQuestions[].questionB").type(JsonFieldType.STRING).description("선택지 B")
                            )
                    ));
        }
    }

    @Test
    @DisplayName("결과 확인 완료 API")
    void markResultViewReady() throws Exception {
        // given
        String roomKey = "ROOM123";
        UserPrincipal mockUser = createMockUser();

        try (MockedStatic<AuthUtil> authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(AuthUtil::getCurrentUser).thenReturn(mockUser);
            doNothing().when(balanceResultService).processGameReady(anyString(), anyLong(), anyString());

            // when & then
            mockMvc.perform(post("/balance/rooms/{roomKey}/votes/ready", roomKey)
                    .header("Authorization", "Bearer valid-token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andDo(document("balance-result-ready",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName("Authorization").description("Bearer 토큰")
                            ),
                            pathParameters(
                                    parameterWithName("roomKey").description("방 키")
                            ),
                            responseFields(
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description("HTTP 상태"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                    fieldWithPath("data").type(JsonFieldType.STRING).description("응답 데이터").optional()
                            )
                    ));
        }
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }
} 