package com.team6.team6.question.controller;

import com.team6.team6.global.ApiResponse;
import com.team6.team6.question.dto.QuestionResponse;
import com.team6.team6.question.service.QuestionService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/questions")
@Validated
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ApiResponse<List<QuestionResponse>> getRandomQuestions(
            @RequestParam @NotBlank(message = "키워드는 비어 있을 수 없습니다.") String keyword
    ) {
        List<QuestionResponse> questions = questionService.getRandomQuestions(keyword);
        return ApiResponse.ok(questions);
    }
}
