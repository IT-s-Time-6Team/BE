package com.team6.team6.balance.dto;

import com.team6.team6.balance.entity.BalanceChoice;
import lombok.Builder;

@Builder
public record BalanceVoteServiceReq(
        Long roomId,
        String roomKey,
        Long memberId,
        String memberName,
        BalanceChoice selectedChoice
) {} 