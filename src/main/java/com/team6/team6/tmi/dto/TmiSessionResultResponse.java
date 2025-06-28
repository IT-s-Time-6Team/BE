package com.team6.team6.tmi.dto;

import java.util.List;

public record TmiSessionResultResponse(
    int correctCount,
    int incorrectCount,
    List<TopVoter> topVoters,
    List<MostIncorrectTmi> mostIncorrectTmis
) {
}
