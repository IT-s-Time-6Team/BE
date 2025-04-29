package com.team6.team6.question.controller;

import com.team6.team6.global.TestSecurityConfig;
import com.team6.team6.question.dto.QuestionResponse;
import com.team6.team6.question.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.team6.team6.global.CustomRestDocsHandler.customDocument;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = QuestionController.class)
@AutoConfigureRestDocs
@Import(TestSecurityConfig.class)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private QuestionService questionService;

    @Test
    void 정상_요청_테스트() throws Exception {
        // given
        String keyword = "LOL";
        List<QuestionResponse> responseList = List.of(
                new QuestionResponse(1L, keyword, "Q1"),
                new QuestionResponse(2L, keyword, "Q2")
        );
        given(questionService.getRandomQuestions(keyword)).willReturn(responseList);

        // when & then
        mockMvc.perform(get("/questions")
                        .param("keyword", keyword))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(customDocument(
                        "get",
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER)
                                        .description("결과 코드"),
                                fieldWithPath("status").type(JsonFieldType.STRING)
                                        .description("HTTP 상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("응답 메시지"),
                                fieldWithPath("data").description("질문 목록"),
                                fieldWithPath("data[].id").description("질문 ID"),
                                fieldWithPath("data[].keyword").description("질문 키워드"),
                                fieldWithPath("data[].question").description("질문 내용")
                        )
                ));
    }
}
