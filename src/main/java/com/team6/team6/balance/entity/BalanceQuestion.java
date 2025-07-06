package com.team6.team6.balance.entity;

import com.team6.team6.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "BALANCE_QUESTION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BalanceQuestion extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "question_a")
    private String questionA;
    
    @Column(name = "question_b") 
    private String questionB;
} 