package com.team6.team6.tmi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.team6.global.security.AuthUtil;
import com.team6.team6.member.entity.CharacterType;
import com.team6.team6.member.security.UserPrincipal;
import com.team6.team6.tmi.dto.*;
import com.team6.team6.tmi.entity.TmiGameStep;
import com.team6.team6.tmi.service.TmiHintService;
import com.team6.team6.tmi.service.TmiSessionService;
import com.team6.team6.tmi.service.TmiSubmitService;
import com.team6.team6.tmi.service.TmiVoteService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TmiController.class)
@AutoConfigureRestDocs
class TmiControllerWebDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private TmiVoteService tmiVoteService;

    @MockitoBean
    private TmiSubmitService tmiSubmitService;

    @MockitoBean
    private TmiSessionService tmiSessionService;

    @MockitoBean
    private TmiHintService tmiHintService;

    private UserPrincipal createMockUser() {
        return new UserPrincipal(1L, "testUser", 1L, "room123", CharacterType.RABBIT, "TMI");
    }

    @Test
    @DisplayName("TMI 제출 API 문서화")
    void submitTmi() throws Exception {
        // given
        TmiSubmitRequest request = new TmiSubmitRequest("나는 어제 3시간만 잤어요");
        UserPrincipal mockUser = createMockUser();

        try (MockedStatic<AuthUtil> authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(AuthUtil::getCurrentUser).thenReturn(mockUser);

            // when & then
            mockMvc.perform(post("/tmi/rooms/{roomKey}/submit", "room123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(document("tmi-submit",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            pathParameters(
                                    parameterWithName("roomKey").description("방 키")
                            ),
                            requestFields(
                                    fieldWithPath("tmiContent").type(JsonFieldType.STRING).description("TMI 내용")
                            ),
                            responseFields(
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description("HTTP 상태"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                    fieldWithPath("data").type(JsonFieldType.STRING).description("성공 메시지")
                            )
                    ));

            verify(tmiSubmitService).submitTmi(any());
        }
    }

    @Test
    @DisplayName("TMI 투표 제출 API 문서화")
    void submitVote() throws Exception {
        // given
        TmiVoteRequest request = new TmiVoteRequest("member1");
        UserPrincipal mockUser = createMockUser();

        try (MockedStatic<AuthUtil> authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(AuthUtil::getCurrentUser).thenReturn(mockUser);

            // when & then
            mockMvc.perform(post("/tmi/rooms/{roomKey}/votes", "room123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(document("tmi-vote-submit",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            pathParameters(
                                    parameterWithName("roomKey").description("방 키")
                            ),
                            requestFields(
                                    fieldWithPath("votedMemberName").type(JsonFieldType.STRING).description("투표할 멤버 이름")
                            ),
                            responseFields(
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description("HTTP 상태"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                    fieldWithPath("data").type(JsonFieldType.STRING).description("성공 메시지")
                            )
                    ));

            verify(tmiVoteService).submitVote(any());
        }
    }

    @Test
    @DisplayName("현재 투표 정보 조회 API 문서화")
    void getCurrentVotingInfo() throws Exception {
        // given
        TmiVotingStartResponse response = new TmiVotingStartResponse(
                "누가 가장 늦게 잠들까요?", 0, List.of("member1", "member2", "member3"));
        UserPrincipal mockUser = createMockUser();

        try (MockedStatic<AuthUtil> authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(AuthUtil::getCurrentUser).thenReturn(mockUser);
            given(tmiVoteService.getCurrentVotingInfo(1L)).willReturn(response);

            // when & then
            mockMvc.perform(get("/tmi/rooms/{roomKey}/votes", "room123"))
                    .andExpect(status().isOk())
                    .andDo(document("tmi-voting-current",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            pathParameters(
                                    parameterWithName("roomKey").description("방 키")
                            ),
                            responseFields(
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description("HTTP 상태"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                    fieldWithPath("data.tmiContent").type(JsonFieldType.STRING).description("TMI 내용"),
                                    fieldWithPath("data.round").type(JsonFieldType.NUMBER).description("라운드"),
                                    fieldWithPath("data.members").type(JsonFieldType.ARRAY).description("투표 가능한 멤버 목록")
                            )
                    ));
        }
    }

    @Test
    @DisplayName("최신 투표 결과 조회 API 문서화")
    void getLatestVotingResult() throws Exception {
        // given
        TmiVotingPersonalResult result = new TmiVotingPersonalResult(
                "누가 가장 늦게 잠들까요?",
                "member1",
                CharacterType.BEAR,
                "testUser",
                CharacterType.CHICK,
                true,
                Map.of("member1", 2L, "member2", 1L),
                0
        );
        UserPrincipal mockUser = createMockUser();

        try (MockedStatic<AuthUtil> authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(AuthUtil::getCurrentUser).thenReturn(mockUser);
            given(tmiVoteService.getLatestVotingResult(1L, "testUser")).willReturn(result);

            // when & then
            mockMvc.perform(get("/tmi/rooms/{roomKey}/votes/result", "room123"))
                    .andExpect(status().isOk())
                    .andDo(document("tmi-voting-result",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            pathParameters(
                                    parameterWithName("roomKey").description("방 키")
                            ),
                            responseFields(
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description("HTTP 상태"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                    fieldWithPath("data.tmiContent").type(JsonFieldType.STRING).description("TMI 내용"),
                                    fieldWithPath("data.correctAnswer").type(JsonFieldType.STRING).description("정답 멤버 이름"),
                                    fieldWithPath("data.answerMemberCharacterType").type(JsonFieldType.STRING).description("정답 멤버 캐릭터"),
                                    fieldWithPath("data.myVote").type(JsonFieldType.STRING).description("내 투표"),
                                    fieldWithPath("data.myCharacterType").type(JsonFieldType.STRING).description("내 투표 캐릭터"),
                                    fieldWithPath("data.isCorrect").type(JsonFieldType.BOOLEAN).description("정답 여부"),
                                    fieldWithPath("data.votingResults").type(JsonFieldType.OBJECT).description("투표 결과"),
                                    fieldWithPath("data.votingResults.*").type(JsonFieldType.NUMBER).description("각 멤버별 득표 수"),
                                    fieldWithPath("data.round").type(JsonFieldType.NUMBER).description("라운드")
                            )
                    ));
        }
    }

    @Test
    @DisplayName("TMI 게임 상태 조회 API 문서화")
    void getGameStatus() throws Exception {
        // given
        TmiSessionStatusResponse response = TmiSessionStatusResponse.builder()
                .currentStep(TmiGameStep.COLLECTING_TMI)
                .hasUserSubmitted(true)
                .build();
        UserPrincipal mockUser = createMockUser();

        try (MockedStatic<AuthUtil> authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(AuthUtil::getCurrentUser).thenReturn(mockUser);
            given(tmiSessionService.getSessionStatus(1L, "testUser")).willReturn(response);

            // when & then
            mockMvc.perform(get("/tmi/rooms/{roomKey}/status", "room123"))
                    .andExpect(status().isOk())
                    .andDo(document("tmi-game-status",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            pathParameters(
                                    parameterWithName("roomKey").description("방 키")
                            ),
                            responseFields(
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description("HTTP 상태"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                    fieldWithPath("data.currentStep").type(JsonFieldType.STRING).description("현재 게임 단계"),
                                    fieldWithPath("data.hasUserSubmitted").type(JsonFieldType.BOOLEAN).description("사용자 제출 여부"),
                                    fieldWithPath("data.progress").type(JsonFieldType.NUMBER).description("진행 상태 (TMI 수집, 투표 단계 외에는 100%로 고정)")
                            )
                    ));
        }
    }

    @Test
    @DisplayName("TMI 게임 최종 결과 조회 API 문서화")
    void getSessionResults() throws Exception {
        // given
        List<TopVoter> topVoters = List.of(
                new TopVoter("member1", 3),
                new TopVoter("member2", 3)
        );

        List<MostIncorrectTmi> mostIncorrectTmis = List.of(
                new MostIncorrectTmi("저는 사실 감자를 싫어해요", 4)
        );

        TmiSessionResultResponse response = new TmiSessionResultResponse(
                3, 2, topVoters, mostIncorrectTmis
        );

        UserPrincipal mockUser = createMockUser();

        try (MockedStatic<AuthUtil> authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(AuthUtil::getCurrentUser).thenReturn(mockUser);
            given(tmiSessionService.getSessionResults(1L, "testUser")).willReturn(response);

            // when & then
            mockMvc.perform(get("/tmi/rooms/{roomKey}/results", "room123"))
                    .andExpect(status().isOk())
                    .andDo(document("tmi-game-results",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            pathParameters(
                                    parameterWithName("roomKey").description("방 키")
                            ),
                            responseFields(
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description("HTTP 상태"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                    fieldWithPath("data.correctCount").type(JsonFieldType.NUMBER).description("맞춘 TMI 개수"),
                                    fieldWithPath("data.incorrectCount").type(JsonFieldType.NUMBER).description("틀린 TMI 개수"),
                                    fieldWithPath("data.topVoters").type(JsonFieldType.ARRAY).description("가장 많은 TMI를 맞춘 사람들"),
                                    fieldWithPath("data.topVoters[].memberName").type(JsonFieldType.STRING).description("멤버 이름"),
                                    fieldWithPath("data.topVoters[].correctCount").type(JsonFieldType.NUMBER).description("맞춘 개수"),
                                    fieldWithPath("data.mostIncorrectTmis").type(JsonFieldType.ARRAY).description("가장 많이 틀린 TMI 목록"),
                                    fieldWithPath("data.mostIncorrectTmis[].tmiContent").type(JsonFieldType.STRING).description("TMI 내용"),
                                    fieldWithPath("data.mostIncorrectTmis[].incorrectVoteCount").type(JsonFieldType.NUMBER).description("틀린 투표 수")
                            )
                    ));

            verify(tmiSessionService).getSessionResults(1L, "testUser");
        }
    }

    @Test
    @DisplayName("힌트 타임 건너뛰기 API 문서화")
    void skipHintTime() throws Exception {
        // given
        UserPrincipal mockUser = createMockUser();

        try (MockedStatic<AuthUtil> authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(AuthUtil::getCurrentUser).thenReturn(mockUser);
            doNothing().when(tmiHintService).skipHintTime(anyString(), anyLong(), anyString());

            // when & then
            mockMvc.perform(post("/tmi/rooms/{roomKey}/hint/skip", "room123"))
                    .andExpect(status().isOk())
                    .andDo(document("tmi-skip-hint",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            pathParameters(
                                    parameterWithName("roomKey").description("방 키")
                            ),
                            responseFields(
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description("HTTP 상태"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                    fieldWithPath("data").type(JsonFieldType.STRING).description("성공 메시지")
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
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll()
                    )
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .build();
        }
    }
}
