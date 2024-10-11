package com.groo83.point.domain.point.dto;

import com.groo83.point.domain.point.PointTransaction;

import com.groo83.point.domain.point.enums.RewardType;
import lombok.*;


@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PointTransactionResDto {

    private Long id;

    private Long memberId;

    private Long orderId;

    private Long amount; // 적립/사용 포인트

    private String expiredDate; // 만료일

    private RewardType rewardType; // 적립 구분

    private Long pointBalance; // 적립/사용 결과 사용자 보유 포인트

    public static PointTransactionResDto fromEntity (PointTransaction pointTransaction, Long pointBalance) {
        return PointTransactionResDto.builder()
                .id(pointTransaction.getId())
                .memberId(pointTransaction.getMemberId())
                .orderId(pointTransaction.getOrderId())
                .amount(pointTransaction.getAmount())
                .rewardType(pointTransaction.getRewardType())
                .expiredDate(String.valueOf(pointTransaction.getExpiredDate()))
                .pointBalance(pointBalance)
                .build();
    }

}
