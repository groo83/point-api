package com.groo83.point.domain.history;

import com.groo83.point.common.entity.BaseEntity;
import com.groo83.point.domain.point.PointTransaction;
import com.groo83.point.domain.point.enums.RewardType;
import com.groo83.point.domain.point.enums.TransactionType;
import com.groo83.point.event.PointEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private Long orderId;

    private Long amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private LocalDateTime expiredDate;

    private RewardType rewardType; // 적립 구분 : 주문/관리자

    @Builder
    public PointHistory(Long memberId, Long orderId, Long amount, TransactionType type, LocalDateTime expiredDate, RewardType rewardType) {
        this.memberId = memberId;
        this.orderId = orderId;
        this.amount = amount;
        this.type = type;
        this.expiredDate = expiredDate;
        this.rewardType = rewardType;
    }

    public static PointHistory createHistory(PointTransaction transaction) {
        return PointHistory.builder()
                .memberId(transaction.getMemberId())
                .orderId(transaction.getOrderId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .expiredDate(transaction.getExpiredDate())
                .rewardType(transaction.getRewardType())
                .build();
    }

    public static PointHistory createUseHistory(PointEvent.CreatePointUseTransaction event) {
        return PointHistory.builder()
                .memberId(event.getMemberId())
                .orderId(event.getOrderId())
                .amount(event.getAmount())
                .type(TransactionType.USE)
                .build();
    }
}
