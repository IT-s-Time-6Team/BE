package com.team6.team6.member.service;

import com.team6.team6.member.domain.MemberRepository;
import com.team6.team6.member.dto.MemberCreateOrLoginServiceRequest;
import com.team6.team6.member.dto.MemberResponse;
import com.team6.team6.member.entity.Member;
import com.team6.team6.member.security.UserPrincipal;
import com.team6.team6.room.entity.Room;
import com.team6.team6.room.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse joinOrLogin(String roomKey, MemberCreateOrLoginServiceRequest request) {
        Room room = roomRepository.findByRoomKeyWithLock(roomKey)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다: " + roomKey));
    
        return memberRepository.findByNicknameAndRoomId(request.nickname(), room.getId())
                .map(member -> login(member, request.password()))
                .orElseGet(() -> join(room, request));
    }

    private MemberResponse login(Member member, String password) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다");
        }

        authenticateUser(member);
        return MemberResponse.from(member);
    }

    private MemberResponse join(Room room, MemberCreateOrLoginServiceRequest request) {
        long currentMemberCount = memberRepository.countByRoomId(room.getId());

        if (currentMemberCount >= room.getMaxMember()) {
            throw new IllegalStateException("방의 최대 인원 수에 도달했습니다. 가입할 수 없습니다.");
        }

        boolean isFirstMember = currentMemberCount == 0;

        // 멤버 순서에 따라 캐릭터 번호 할당 (1부터 시작, 8 이상은 미지정으로 처리됨)
        int characterOrder = (int) currentMemberCount + 1;

        Member newMember = Member.create(
                request.nickname(),
                passwordEncoder.encode(request.password()),
                room,
                characterOrder,
                isFirstMember
        );

        Member savedMember = memberRepository.save(newMember);
        authenticateUser(savedMember);
        return MemberResponse.from(savedMember);
    }

    private void authenticateUser(Member member) {
        UserPrincipal userPrincipal = new UserPrincipal(member);
        
        // 권한 목록 생성
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        // 첫 번째 멤버(리더)인 경우 추가 권한 부여
        if (member.isLeader()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_LEADER"));
        }
        
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                authorities
        );

        // SecurityContext에 인증 객체 저장
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}