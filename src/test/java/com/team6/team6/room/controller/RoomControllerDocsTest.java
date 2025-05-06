package com.team6.team6.room.controller;

import com.team6.team6.global.CustomRestDocsHandler;
import com.team6.team6.global.RestDocsSupport;
import com.team6.team6.room.dto.RoomCreateRequest;
import com.team6.team6.room.dto.RoomResponse;
import com.team6.team6.room.dto.RoomResult;
import com.team6.team6.room.entity.GameMode;
import com.team6.team6.room.service.RoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RoomControllerDocsTest extends RestDocsSupport {

    private final RoomService roomService = mock(RoomService.class);

    @Override
    protected Object initController() {
        return new RoomController(roomService);
    }

    @DisplayName("방 생성 API")
    @Test
    void createRoom() throws Exception {
        RoomResponse mockResponse = RoomResponse.builder()
                .roomKey("abc123")
                .requiredAgreements(3)
                .maxMember(6)
                .durationMinutes(30)
                .gameMode(GameMode.NORMAL)
                .createdAt(LocalDateTime.now())
                .closedAt(null)
                .isClosed(false)
                .build();

        given(roomService.createRoom(any())).willReturn(mockResponse);

        RoomCreateRequest request = new RoomCreateRequest(3, 6, 30, GameMode.NORMAL);

        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(CustomRestDocsHandler.customDocument("create",
                        requestFields(
                                fieldWithPath("requiredAgreements").type(JsonFieldType.NUMBER)
                                        .description("공감 기준 인원 (필수, 최소 2명, 최대 20명)"),
                                fieldWithPath("maxMember").type(JsonFieldType.NUMBER)
                                        .description("최대 입장 인원 (필수, 최소 2명, 최대 20명)"),
                                fieldWithPath("durationMinutes").type(JsonFieldType.NUMBER)
                                        .optional()
                                        .description("시간 제한(분), 미입력 시 기본 30분 (최소 5분, 최대 360분)"),
                                fieldWithPath("gameMode").type(JsonFieldType.STRING)
                                        .description("게임 모드 (필수, NORMAL)")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER)
                                        .description("결과 코드"),
                                fieldWithPath("status").type(JsonFieldType.STRING)
                                        .description("HTTP 상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("응답 메시지"),
                                fieldWithPath("data.roomKey").type(JsonFieldType.STRING)
                                        .description("방 키"),
                                fieldWithPath("data.requiredAgreements").type(JsonFieldType.NUMBER)
                                        .description("공감 기준 인원"),
                                fieldWithPath("data.maxMember").type(JsonFieldType.NUMBER)
                                        .description("최대 입장 인원"),
                                fieldWithPath("data.durationMinutes").type(JsonFieldType.NUMBER)
                                        .description("시간 제한(분)"),
                                fieldWithPath("data.gameMode").type(JsonFieldType.STRING)
                                        .description("게임 모드"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING)
                                        .description("생성 시각"),
                                fieldWithPath("data.closedAt").type(JsonFieldType.NULL)
                                        .description("종료 시각"),
                                fieldWithPath("data.isClosed").type(JsonFieldType.BOOLEAN)
                                        .description("종료 여부")
                        )
                ));
    }

    @DisplayName("방 조회 API")
    @Test
    void getRoom() throws Exception {
        RoomResponse mockResponse = RoomResponse.builder()
                .roomKey("abc123")
                .requiredAgreements(3)
                .maxMember(6)
                .durationMinutes(30)
                .gameMode(GameMode.NORMAL)
                .createdAt(LocalDateTime.now())
                .closedAt(null)
                .isClosed(false)
                .build();

        given(roomService.getRoom("abc123")).willReturn(mockResponse);

        mockMvc.perform(get("/rooms/{roomKey}", "abc123")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(CustomRestDocsHandler.customDocument("get",
                        pathParameters(
                                parameterWithName("roomKey").description("방 키")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER)
                                        .description("결과 코드"),
                                fieldWithPath("status").type(JsonFieldType.STRING)
                                        .description("HTTP 상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("응답 메시지"),
                                fieldWithPath("data.roomKey").type(JsonFieldType.STRING)
                                        .description("방 키"),
                                fieldWithPath("data.requiredAgreements").type(JsonFieldType.NUMBER)
                                        .description("공감 기준 인원"),
                                fieldWithPath("data.maxMember").type(JsonFieldType.NUMBER)
                                        .description("최대 입장 인원"),
                                fieldWithPath("data.durationMinutes").type(JsonFieldType.NUMBER)
                                        .description("시간 제한(분)"),
                                fieldWithPath("data.gameMode").type(JsonFieldType.STRING)
                                        .description("게임 모드"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING)
                                        .description("생성 시각"),
                                fieldWithPath("data.closedAt").type(JsonFieldType.STRING)
                                        .optional()
                                        .description("종료 시각"),
                                fieldWithPath("data.isClosed").type(JsonFieldType.BOOLEAN)
                                        .description("종료 여부")
                        )
                ));
    }

    @DisplayName("방 종료 API")
    @Test
    void closeRoom() throws Exception {
        willDoNothing().given(roomService).closeRoom("abc123");

        mockMvc.perform(patch("/rooms/{roomKey}/close", "abc123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(CustomRestDocsHandler.customDocument("close",
                        pathParameters(
                                parameterWithName("roomKey").description("방 키")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER)
                                        .description("결과 코드"),
                                fieldWithPath("status").type(JsonFieldType.STRING)
                                        .description("HTTP 상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL)
                                        .description("응답 데이터 없음")
                        )
                ));
    }

    @DisplayName("방 결과 조회 API")
    @Test
    void getRoomResult() throws Exception {
        // 가짜 RoomResult 객체 생성
        List<String> sharedKeywords = List.of("LOL", "애니");
        List<String> topContributors = List.of("하나");
        List<String> mostMatchedUsers = List.of("하나");

        RoomResult mockResult = new RoomResult(
                sharedKeywords,
                "30분 12초",
                topContributors,
                3,
                mostMatchedUsers,
                2
        );

        given(roomService.getRoomResult("abc123")).willReturn(mockResult);

        mockMvc.perform(get("/rooms/{roomKey}/result", "abc123")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(CustomRestDocsHandler.customDocument("get-result",
                        pathParameters(
                                parameterWithName("roomKey").description("방 키")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER)
                                        .description("결과 코드"),
                                fieldWithPath("status").type(JsonFieldType.STRING)
                                        .description("HTTP 상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("응답 메시지"),
                                fieldWithPath("data.sharedKeywords").type(JsonFieldType.ARRAY)
                                        .description("공유 키워드 목록"),
                                fieldWithPath("data.totalDuration").type(JsonFieldType.STRING)
                                        .description("총 대화 시간"),
                                fieldWithPath("data.topKeywordContributorNames").type(JsonFieldType.ARRAY)
                                        .description("가장 많은 키워드를 생성한 사용자들"),
                                fieldWithPath("data.topKeywordCount").type(JsonFieldType.NUMBER)
                                        .description("최다 키워드 생성 수"),
                                fieldWithPath("data.mostMatchedHobbyUserNames").type(JsonFieldType.ARRAY)
                                        .description("공감 키워드가 가장 많은 사용자들"),
                                fieldWithPath("data.matchedHobbyCount").type(JsonFieldType.NUMBER)
                                        .description("최다 공감 키워드 수")
                        )
                ));
    }
}
