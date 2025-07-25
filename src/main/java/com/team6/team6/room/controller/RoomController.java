package com.team6.team6.room.controller;

import com.team6.team6.global.ApiResponse;
import com.team6.team6.room.dto.KeywordRoomResult;
import com.team6.team6.room.dto.RoomCreateRequest;
import com.team6.team6.room.dto.RoomResponse;
import com.team6.team6.room.service.ResultService;
import com.team6.team6.room.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private final ResultService resultService;

    // 방 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoomResponse> createRoom(@Valid @RequestBody RoomCreateRequest request) {

        RoomResponse createdRoom = roomService.createRoom(request.toServiceRequest());
        return ApiResponse.of(HttpStatus.CREATED, "방이 생성되었습니다", createdRoom);
    }

    // 방 조회
    @GetMapping("/{roomKey}")
    public ApiResponse<RoomResponse> getRoom(@PathVariable String roomKey) {
        RoomResponse room = roomService.getRoom(roomKey);
        return ApiResponse.of(HttpStatus.OK, "방 정보 조회 성공", room);
    }


    // 방 종료
    @PatchMapping("/{roomKey}/close")
    public ApiResponse<Void> closeRoom(@PathVariable String roomKey) {
        roomService.closeRoom(roomKey);
        return ApiResponse.of(HttpStatus.OK, "방이 종료되었습니다", null);
    }

    // 키워드 결과 조회
    @GetMapping("/{roomKey}/result")
    public ApiResponse<KeywordRoomResult> getKeywordResult(@PathVariable String roomKey) {
        KeywordRoomResult roomResult = resultService.getKeywordResult(roomKey);
        return ApiResponse.of(HttpStatus.OK, "키워드 결과 조회 성공", roomResult);
    }
}
