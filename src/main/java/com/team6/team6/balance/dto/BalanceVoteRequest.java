package com.team6.team6.balance.dto;

import com.team6.team6.balance.entity.BalanceChoice;
import jakarta.validation.constraints.NotNull;

public record BalanceVoteRequest(
        @NotNull(message = "선택지를 선택해주세요")
        BalanceChoice selectedChoice
) {
    
    public BalanceVoteServiceReq toServiceRequest(Long roomId, String roomKey, 
                                                 Long memberId, String memberName) {
        return BalanceVoteServiceReq.builder()
                .roomId(roomId)
                .roomKey(roomKey)
                .memberId(memberId)
                .memberName(memberName)
                .selectedChoice(selectedChoice)
                .build();
    }
} 