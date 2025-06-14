package com.team6.team6.global.config;

import com.team6.team6.keyword.dto.KewordChatMessage;
import com.team6.team6.member.domain.MemberRepository;
import com.team6.team6.member.dto.MemberCreateOrLoginRequest;
import com.team6.team6.room.entity.GameMode;
import com.team6.team6.room.entity.Room;
import com.team6.team6.room.repository.RoomRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketAuthorizationIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private MemberRepository memberRepository;

    private WebSocketStompClient stompClient;
    private String wsUrl;

    private String room1Key;
    private String room2Key;

    private static StompFrameHandler getStompFrameHandler() {
        return new StompFrameHandler() {
            public Type getPayloadType(StompHeaders headers) {
                return KewordChatMessage.class;
            }

            public void handleFrame(StompHeaders headers, Object payload) {
            }
        };
    }

    @BeforeEach
    void setUp() {

        wsUrl = "ws://localhost:" + port + "/connect";
        stompClient = new WebSocketStompClient(new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))
        ));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        // 테스트 데이터 생성
        Room room1 = roomRepository.save(Room.builder()
                .roomKey("room1Key").requiredAgreements(3).maxMember(6).durationMinutes(30).gameMode(GameMode.NORMAL).build());
        Room room2 = roomRepository.save(Room.builder()
                .roomKey("room2Key").requiredAgreements(3).maxMember(6).durationMinutes(30).gameMode(GameMode.NORMAL).build());

        room1Key = room1.getRoomKey();
        room2Key = room2.getRoomKey();
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
        roomRepository.deleteAll();
    }

    private ResponseEntity<String> loginToRoom(String roomKey, MemberCreateOrLoginRequest loginRequest) {
        RestTemplate restTemplate = new RestTemplate();
        String loginUrl = "http://localhost:" + port + "/rooms/" + roomKey + "/member";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MemberCreateOrLoginRequest> request = new HttpEntity<>(loginRequest, headers);

        return restTemplate.postForEntity(loginUrl, request, String.class);
    }

    private String extractSessionCookie(ResponseEntity<String> response) {
        List<String> cookies = response.getHeaders().get("Set-Cookie");
        if (cookies == null || cookies.isEmpty()) {
            return null;
        }

        return cookies.stream()
                .filter(c -> c.startsWith("JSESSIONID"))
                .findFirst()
                .orElse(null);
    }

    private StompSession connectToStompServerWithAuth(String roomKey, MemberCreateOrLoginRequest loginRequest) throws Exception {
        ResponseEntity<String> loginResponse = loginToRoom(roomKey, loginRequest);
        String sessionCookie = extractSessionCookie(loginResponse);

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Cookie", sessionCookie);

        return stompClient.connectAsync(wsUrl, headers, new StompHeaders(),
                new StompSessionHandlerAdapter() {
                }).get(5, TimeUnit.SECONDS);
    }

    @Test
    void 인증된_사용자_본인방_구독_성공() throws Exception {
        // Given
        MemberCreateOrLoginRequest loginRequest = new MemberCreateOrLoginRequest("user1", "testPassword@");
        StompSession session = connectToStompServerWithAuth(room1Key, loginRequest);

        // When
        session.subscribe("/topic/room/" + room1Key + "/messages", getStompFrameHandler());

        TimeUnit.MILLISECONDS.sleep(500);

        // Then
        assertThat(session.isConnected()).isTrue();
    }

    @Test
    void 인증된_사용자_다른방_구독_실패() throws Exception {
        // Given
        MemberCreateOrLoginRequest loginRequest = new MemberCreateOrLoginRequest("user1", "testPassword@");
        StompSession session = connectToStompServerWithAuth(room1Key, loginRequest);

        // When
        session.subscribe("/topic/room/" + room2Key + "/messages", getStompFrameHandler());
        TimeUnit.MILLISECONDS.sleep(500);

        // Then
        assertThat(session.isConnected()).isFalse();
    }

    @Test
    void 인증된_사용자_본인방_메시지전송_성공() throws Exception {
        // Given
        MemberCreateOrLoginRequest loginRequest = new MemberCreateOrLoginRequest("user1", "testPassword@");
        StompSession session = connectToStompServerWithAuth(room1Key, loginRequest);

        // When
        session.send("/app/room/" + room1Key + "/keyword", Map.of("keyword", "테스트"));
        TimeUnit.MILLISECONDS.sleep(500);

        // Then
        assertThat(session.isConnected()).isTrue();
    }

    @Test
    void 인증된_사용자_다른방_메시지전송_실패() throws Exception {
        // Given
        MemberCreateOrLoginRequest loginRequest = new MemberCreateOrLoginRequest("user1", "testPassword@");
        StompSession session = connectToStompServerWithAuth(room1Key, loginRequest);

        // When
        session.send("/app/room/" + room2Key + "/keyword", Map.of("keyword", "테스트"));
        TimeUnit.MILLISECONDS.sleep(500);

        // Then
        assertThat(session.isConnected()).isFalse();
    }

    @Test
    void 미인증_사용자_접근_실패() throws Exception {
        // Given
        SecurityContextHolder.clearContext();
        AtomicReference<Throwable> exception = new AtomicReference<>();

        // When
        stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {
            @Override
            public void handleTransportError(StompSession session, Throwable ex) {
                exception.set(ex);
            }
        });
        TimeUnit.MILLISECONDS.sleep(500);

        // Then
        assertThat(exception.get()).isNotNull();
    }
}
