package com.team6.team6;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping
    public ResponseEntity<TestResponseDto> getTest() {
        return ResponseEntity.ok(new TestResponseDto("testStr", 1));
    }

    @PostMapping
    public ResponseEntity<TestResponseDto> postTest(@RequestBody TestRequestDto request) {
        return ResponseEntity.ok(new TestResponseDto(request.testStr(), request.testInt()));
    }
}
