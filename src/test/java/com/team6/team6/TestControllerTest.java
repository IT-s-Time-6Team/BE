package com.team6.team6;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.team6.global.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

import static com.team6.team6.global.CustomRestDocsHandler.customDocument;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureRestDocs
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Test GET 테스트")
    void getTest() throws Exception {
        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andDo(customDocument("get-test",
                        responseFields(
                                fieldWithPath("testStr").description("이름"),
                                fieldWithPath("testInt").description("나이")
                        )
                ));
    }

    @Test
    @DisplayName("Test POST 테스트")
    void postTest() throws Exception {
        // given
        TestRequestDto request = new TestRequestDto("hello", 123);

        // when & then
        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(customDocument("post-test",
                        requestFields(
                                fieldWithPath("testStr").type(JsonFieldType.STRING).description("이름"),
                                fieldWithPath("testInt").type(JsonFieldType.NUMBER).description("나이")
                        ),
                        responseFields(
                                fieldWithPath("testStr").description("이름"),
                                fieldWithPath("testInt").description("나이")
                        )));
    }
}
