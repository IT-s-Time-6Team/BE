package com.team6.team6.member.controller;

import com.team6.team6.global.config.SecurityConfig;
import com.team6.team6.member.dto.MemberResponse;
import com.team6.team6.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.team6.team6.global.CustomRestDocsHandler.customDocument;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemberController.class)
@AutoConfigureRestDocs
@Import(SecurityConfig.class)
class MemberControllerDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    @Test
    void 회원_가입_또는_로그인_정상_요청_테스트() throws Exception {
        // given
        String roomKey = "room123";
        MemberResponse memberResponse = new MemberResponse("tester", 1, true);
        
        given(memberService.joinOrLogin(any(), any())).willReturn(memberResponse);

        // Request body
        String requestBody = """
                {
                    "nickname": "tester",
                    "password": "test123!"
                }
                """;

        // when & then
        mockMvc.perform(post("/rooms/{roomKey}/member", roomKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(customDocument(
                        "join-or-login",
                        pathParameters(
                                parameterWithName("roomKey").description("방 고유 키")
                        ),
                        requestFields(
                                fieldWithPath("nickname").type(JsonFieldType.STRING)
                                        .description("사용자 닉네임"),
                                fieldWithPath("password").type(JsonFieldType.STRING)
                                        .description("사용자 비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER)
                                        .description("결과 코드"),
                                fieldWithPath("status").type(JsonFieldType.STRING)
                                        .description("HTTP 상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("응답 메시지"),
                                fieldWithPath("data").description("회원 정보"),
                                fieldWithPath("data.nickname").description("회원 닉네임"),
                                fieldWithPath("data.characterId").description("캐릭터 ID"),
                                fieldWithPath("data.isLeader").description("방장 여부")
                        )
                ));
    }

    @Test
    void 회원_가입_또는_로그인_유효성_검증_실패_테스트() throws Exception {
        // given
        String roomKey = "room123";

        // Request body with invalid data
        String requestBody = """
                {
                    "nickname": "",
                    "password": "123"
                }
                """;

        // when & then
        mockMvc.perform(post("/rooms/{roomKey}/member", roomKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(customDocument(
                        "join-or-login-validation-failure",
                        pathParameters(
                                parameterWithName("roomKey").description("방 고유 키")
                        ),
                        requestFields(
                                fieldWithPath("nickname").type(JsonFieldType.STRING)
                                        .description("사용자 닉네임"),
                                fieldWithPath("password").type(JsonFieldType.STRING)
                                        .description("사용자 비밀번호")
                        )
                ));
    }
}