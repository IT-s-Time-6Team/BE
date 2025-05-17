package com.team6.team6.member.service;

import com.team6.team6.member.domain.MemberRepository;
import com.team6.team6.member.dto.MemberCreateOrLoginServiceRequest;
import com.team6.team6.member.dto.MemberResponse;
import com.team6.team6.member.entity.CharacterType;
import com.team6.team6.member.entity.Member;
import com.team6.team6.member.security.UserPrincipal;
import com.team6.team6.room.dto.RoomCreateServiceRequest;
import com.team6.team6.room.dto.RoomResponse;
import com.team6.team6.room.entity.GameMode;
import com.team6.team6.room.entity.Room;
import com.team6.team6.room.repository.RoomRepository;
import com.team6.team6.room.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void 신규_멤버_가입_성공() {
        // given
        RoomCreateServiceRequest roomRequest = new RoomCreateServiceRequest(
                3, 6, 30, GameMode.NORMAL
        );
        RoomResponse roomResponse = roomService.createRoom(roomRequest);
        String roomKey = roomResponse.roomKey();

        MemberCreateOrLoginServiceRequest memberRequest = MemberCreateOrLoginServiceRequest.builder()
                .nickname("테스트유저")
                .password("test123!")
                .build();

        // when
        MemberResponse response = memberService.joinOrLogin(roomKey, memberRequest);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response).isNotNull();
            softly.assertThat(response.nickname()).isEqualTo("테스트유저");
            softly.assertThat(response.character()).isEqualTo(CharacterType.RABBIT); // CharacterType.RABBIT로 변경
            softly.assertThat(response.isLeader()).isTrue(); // 첫 번째 멤버는 리더

            // 실제 DB에 저장되었는지 확인
            Room room = roomRepository.findByRoomKey(roomKey).orElseThrow();
            Member member = memberRepository.findByNicknameAndRoomId("테스트유저", room.getId()).orElseThrow();
            softly.assertThat(member.isLeader()).isTrue();

            // 인증 상태 확인
            softly.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        });
    }
    
    @Test
    void 기존_멤버_로그인_성공() {
        // given
        RoomCreateServiceRequest roomRequest = new RoomCreateServiceRequest(
                3, 6, 30, GameMode.NORMAL
        );
        RoomResponse roomResponse = roomService.createRoom(roomRequest);
        String roomKey = roomResponse.roomKey();
        
        MemberCreateOrLoginServiceRequest memberRequest = MemberCreateOrLoginServiceRequest.builder()
                .nickname("테스트유저")
                .password("test123!")
                .build();
                
        // 먼저 회원가입
        memberService.joinOrLogin(roomKey, memberRequest);
        
        
        // when - 동일한 정보로 로그인
        MemberResponse response = memberService.joinOrLogin(roomKey, memberRequest);
        
        // then
        assertSoftly(softly -> {
            softly.assertThat(response).isNotNull();
            softly.assertThat(response.nickname()).isEqualTo("테스트유저");
            softly.assertThat(response.isLeader()).isTrue();
            
            // 인증 상태 확인
            softly.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        });
    }
    
    @Test
    void 로그인_비밀번호_불일치시_예외발생() {
        // given
        RoomCreateServiceRequest roomRequest = new RoomCreateServiceRequest(
                3, 6, 30, GameMode.NORMAL
        );
        RoomResponse roomResponse = roomService.createRoom(roomRequest);
        String roomKey = roomResponse.roomKey();
        
        // 먼저 회원가입
        MemberCreateOrLoginServiceRequest joinRequest = MemberCreateOrLoginServiceRequest.builder()
                .nickname("테스트유저")
                .password("test123!")
                .build();
        memberService.joinOrLogin(roomKey, joinRequest);
        
        // 잘못된 비밀번호로 로그인 시도
        MemberCreateOrLoginServiceRequest loginRequest = MemberCreateOrLoginServiceRequest.builder()
                .nickname("테스트유저")
                .password("wrong123!")
                .build();
        
        // when & then
        assertThatThrownBy(() -> memberService.joinOrLogin(roomKey, loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다");
    }
    
    @Test
    void 존재하지_않는_방_멤버_가입시_예외발생() {
        // given
        String nonExistentRoomKey = "non-existent-key";
        
        MemberCreateOrLoginServiceRequest memberRequest = MemberCreateOrLoginServiceRequest.builder()
                .nickname("테스트유저")
                .password("test123!")
                .build();
        
        // when & then
        assertThatThrownBy(() -> memberService.joinOrLogin(nonExistentRoomKey, memberRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("방을 찾을 수 없습니다");
    }
    
    @Test
    void 방_최대인원_초과시_가입_실패() {
        // given
        RoomCreateServiceRequest roomRequest = new RoomCreateServiceRequest(
                3, 2, 30, GameMode.NORMAL // 최대 인원 2명으로 설정
        );
        RoomResponse roomResponse = roomService.createRoom(roomRequest);
        String roomKey = roomResponse.roomKey();
        
        // 첫 번째 멤버 가입
        MemberCreateOrLoginServiceRequest member1Request = MemberCreateOrLoginServiceRequest.builder()
                .nickname("테스트유저1")
                .password("test123!")
                .build();
        memberService.joinOrLogin(roomKey, member1Request);
        
        // 두 번째 멤버 가입
        MemberCreateOrLoginServiceRequest member2Request = MemberCreateOrLoginServiceRequest.builder()
                .nickname("테스트유저2")
                .password("test123!")
                .build();
        memberService.joinOrLogin(roomKey, member2Request);
        
        // 세 번째 멤버 가입 시도 (최대 인원 초과)
        MemberCreateOrLoginServiceRequest member3Request = MemberCreateOrLoginServiceRequest.builder()
                .nickname("테스트유저3")
                .password("test123!")
                .build();
        
        // when & then
        assertThatThrownBy(() -> memberService.joinOrLogin(roomKey, member3Request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("방의 최대 인원 수에 도달했습니다");
    }
    
    @Test
    void 두번째_이후_멤버는_리더가_아님() {
        // given
        RoomCreateServiceRequest roomRequest = new RoomCreateServiceRequest(
                3, 3, 30, GameMode.NORMAL
        );
        RoomResponse roomResponse = roomService.createRoom(roomRequest);
        String roomKey = roomResponse.roomKey();
        
        // 첫 번째 멤버 가입
        MemberCreateOrLoginServiceRequest member1Request = MemberCreateOrLoginServiceRequest.builder()
                .nickname("테스트유저1")
                .password("test123!")
                .build();
        MemberResponse response1 = memberService.joinOrLogin(roomKey, member1Request);
        
        // 두 번째 멤버 가입
        MemberCreateOrLoginServiceRequest member2Request = MemberCreateOrLoginServiceRequest.builder()
                .nickname("테스트유저2")
                .password("test123!")
                .build();
        
        // when
        MemberResponse response2 = memberService.joinOrLogin(roomKey, member2Request);
        
        // then
        assertSoftly(softly -> {
            softly.assertThat(response1.isLeader()).isTrue(); // 첫 번째 멤버는 리더
            softly.assertThat(response2.isLeader()).isFalse(); // 두 번째 멤버는 리더가 아님
        });
    }
    
    @Test
    void 닉네임_중복_확인() {
        // given
        RoomCreateServiceRequest roomRequest = new RoomCreateServiceRequest(
                3, 6, 30, GameMode.NORMAL
        );
        RoomResponse roomResponse = roomService.createRoom(roomRequest);
        String roomKey = roomResponse.roomKey();
        
        // 첫 번째 멤버 가입
        MemberCreateOrLoginServiceRequest memberRequest = MemberCreateOrLoginServiceRequest.builder()
                .nickname("중복닉네임")
                .password("test123!")
                .build();
        
        memberService.joinOrLogin(roomKey, memberRequest);
        
        
        // 다른 비밀번호로 동일 닉네임 가입시도
        MemberCreateOrLoginServiceRequest duplicateRequest = MemberCreateOrLoginServiceRequest.builder()
                .nickname("중복닉네임")
                .password("different!")
                .build();
        
        // when & then
        assertThatThrownBy(() -> memberService.joinOrLogin(roomKey, duplicateRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다");
    }
    
    @Test
    void 인증_컨텍스트_검증() {
        // given
        RoomCreateServiceRequest roomRequest = new RoomCreateServiceRequest(
                3, 6, 30, GameMode.NORMAL
        );
        RoomResponse roomResponse = roomService.createRoom(roomRequest);
        String roomKey = roomResponse.roomKey();

        MemberCreateOrLoginServiceRequest memberRequest = MemberCreateOrLoginServiceRequest.builder()
                .nickname("테스트유저")
                .password("test123!")
                .build();

        // when
        memberService.joinOrLogin(roomKey, memberRequest);

        // then
        assertSoftly(softly -> {
            // 인증 객체가 존재하는지 확인
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            softly.assertThat(authentication).isNotNull();

            // Principal이 올바른 타입인지 확인
            softly.assertThat(authentication.getPrincipal()).isInstanceOf(UserPrincipal.class);

            // UserPrincipal에 올바른 정보가 들어있는지 확인
            var principal = (UserPrincipal) authentication.getPrincipal();
            softly.assertThat(principal.getNickname()).isEqualTo("테스트유저");
            softly.assertThat(principal.getRoomKey()).isEqualTo(roomKey);

            // 권한(Authorities) 확인 - 첫 번째 멤버는 ROLE_USER와 ROLE_LEADER 모두 가짐
            softly.assertThat(authentication.getAuthorities()).hasSize(2);

            // 권한 목록에 ROLE_USER와 ROLE_LEADER가 모두 포함되어 있는지 확인
            var authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            softly.assertThat(authorities).contains("ROLE_USER", "ROLE_LEADER");
        });
    }

    @Test
    void 로그인_시_보안_컨텍스트_갱신() {
        // given
        RoomCreateServiceRequest roomRequest = new RoomCreateServiceRequest(
                3, 6, 30, GameMode.NORMAL
        );
        RoomResponse roomResponse = roomService.createRoom(roomRequest);
        String roomKey = roomResponse.roomKey();
        
        // 첫 번째 멤버 가입
        MemberCreateOrLoginServiceRequest member1Request = MemberCreateOrLoginServiceRequest.builder()
                .nickname("유저1")
                .password("test123!")
                .build();
        memberService.joinOrLogin(roomKey, member1Request);
        
        // 명시적으로 SecurityContext 초기화
        SecurityContextHolder.clearContext();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        
        // 두 번째 멤버 가입
        MemberCreateOrLoginServiceRequest member2Request = MemberCreateOrLoginServiceRequest.builder()
                .nickname("유저2")
                .password("test123!")
                .build();
        
        // when
        memberService.joinOrLogin(roomKey, member2Request);
        
        // then
        assertSoftly(softly -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            softly.assertThat(authentication).isNotNull();
            
            var principal = (UserPrincipal) authentication.getPrincipal();
            softly.assertThat(principal.getNickname()).isEqualTo("유저2");
            
            // 이전 사용자가 아닌 현재 사용자 정보가 들어있는지 확인
            softly.assertThat(principal.getNickname()).isNotEqualTo("유저1");
        });
    }
    
    @Test
    void 보안_컨텍스트_초기화_후_로그인() {
        // given
        RoomCreateServiceRequest roomRequest = new RoomCreateServiceRequest(
                3, 6, 30, GameMode.NORMAL
        );
        RoomResponse roomResponse = roomService.createRoom(roomRequest);
        String roomKey = roomResponse.roomKey();
        
        MemberCreateOrLoginServiceRequest memberRequest = MemberCreateOrLoginServiceRequest.builder()
                .nickname("테스트유저")
                .password("test123!")
                .build();
        
        // 첫 번째 로그인
        memberService.joinOrLogin(roomKey, memberRequest);
        
        // 명시적 컨텍스트 초기화
        SecurityContextHolder.clearContext();
        
        // when - 두 번째 로그인
        memberService.joinOrLogin(roomKey, memberRequest);
        
        // then
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertSoftly(softly -> {
            softly.assertThat(authentication).isNotNull();
            softly.assertThat(authentication.getPrincipal()).isInstanceOf(UserPrincipal.class);
            
            var principal = (UserPrincipal) authentication.getPrincipal();
            softly.assertThat(principal.getNickname()).isEqualTo("테스트유저");
        });
    }
    
    @Test
    void 첫번째_멤버는_리더_권한_가짐() {
        // given
        RoomCreateServiceRequest roomRequest = new RoomCreateServiceRequest(
                3, 6, 30, GameMode.NORMAL
        );
        RoomResponse roomResponse = roomService.createRoom(roomRequest);
        String roomKey = roomResponse.roomKey();
        
        MemberCreateOrLoginServiceRequest leaderRequest = MemberCreateOrLoginServiceRequest.builder()
                .nickname("방장")
                .password("test123!")
                .build();
                
        // when
        memberService.joinOrLogin(roomKey, leaderRequest);
        
        // then
        assertSoftly(softly -> {
            // 인증 객체가 존재하는지 확인
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            softly.assertThat(authentication).isNotNull();
            
            // 권한(Authorities) 확인 - 리더는 ROLE_USER와 ROLE_LEADER 두 권한을 가짐
            softly.assertThat(authentication.getAuthorities()).hasSize(2);
            
            // 권한 목록에 ROLE_USER와 ROLE_LEADER가 모두 포함되어 있는지 확인
            var authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            softly.assertThat(authorities).contains("ROLE_USER", "ROLE_LEADER");
        });
    }
    
    @Test
    void 두번째_멤버는_일반_권한만_가짐() {
        // given
        RoomCreateServiceRequest roomRequest = new RoomCreateServiceRequest(
                3, 6, 30, GameMode.NORMAL
        );
        RoomResponse roomResponse = roomService.createRoom(roomRequest);
        String roomKey = roomResponse.roomKey();
        
        // 첫 번째 멤버 가입(리더)
        MemberCreateOrLoginServiceRequest leaderRequest = MemberCreateOrLoginServiceRequest.builder()
                .nickname("방장")
                .password("test123!")
                .build();
        memberService.joinOrLogin(roomKey, leaderRequest);
        
        // SecurityContext 초기화
        SecurityContextHolder.clearContext();
        
        // 두 번째 멤버 가입(일반 멤버)
        MemberCreateOrLoginServiceRequest memberRequest = MemberCreateOrLoginServiceRequest.builder()
                .nickname("일반멤버")
                .password("test123!")
                .build();
        
        // when
        memberService.joinOrLogin(roomKey, memberRequest);
        
        // then
        assertSoftly(softly -> {
            // 인증 객체가 존재하는지 확인
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            softly.assertThat(authentication).isNotNull();
            
            // 권한(Authorities) 확인 - 일반 멤버는 ROLE_USER 권한만 가짐
            softly.assertThat(authentication.getAuthorities()).hasSize(1);
            
            // 권한 목록에 ROLE_USER만 포함되어 있는지 확인
            var authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            softly.assertThat(authorities).contains("ROLE_USER");
            softly.assertThat(authorities).doesNotContain("ROLE_LEADER");
        });
    }
}