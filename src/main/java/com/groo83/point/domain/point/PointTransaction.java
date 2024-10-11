package com.groo83.point.domain.point;

import com.groo83.point.common.entity.BaseEntity;
import com.groo83.point.domain.point.dto.PointSaveReqDto;
import com.groo83.point.domain.point.dto.PointUseReqDto;
import com.groo83.point.domain.point.enums.RewardType;
import com.groo83.point.domain.point.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "point_transaction", indexes = {
    @Index(name = "idx_member_id", columnList = "memberId"),
    @Index(name = "idx_member_id_order_id", columnList = "memberId, orderId"),
    @Index(name = "idx_member_id_expired_date_type", columnList = "memberId, expiredDate, type")

})
public class PointTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private Long orderId;

    private Long amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private LocalDateTime expiredDate;

    private Long usedPointId;     // 포인트 사용 시 사용된 적립 포인트의 ID

    private Long cancelPointId;   // 포인트 사용 취소 시 취소 대상 사용 포인트의 ID

    private Long originalPointId; // 포인트 적립 취소 시 원래 적립된 포인트의 ID

    @Enumerated(EnumType.STRING)
    private RewardType rewardType; // 적립 구분 : 주문/관리자

    @Version
    private Long version;

    @Builder
    public PointTransaction(Long memberId, Long orderId, Long amount, TransactionType type, LocalDateTime expiredDate, Long usedPointId, Long cancelPointId, Long originalPointId, RewardType rewardType) {
        this.memberId = memberId;
        this.orderId = orderId;
        this.amount = amount;
        this.type = type;
        this.expiredDate = expiredDate;
        this.usedPointId = usedPointId;
        this.cancelPointId = cancelPointId;
        this.originalPointId = originalPointId;
        this.rewardType = rewardType;
    }

    public static PointTransaction createSavePoint(Long memberId, PointSaveReqDto reqDto) {

        return PointTransaction.builder()
                .memberId(memberId)
                .type(TransactionType.SAVE)
                .rewardType(RewardType.of(reqDto.getRewardType()))
                .amount(reqDto.getAmount())
                .orderId(reqDto.getOrderId())
                .expiredDate(reqDto.getExpiredDate() != null ?
                        reqDto.getExpiredDate() :
                        LocalDateTime.now().plusDays(364))  // 기본값 설정은 엔티티에서 처리
                .build();
    }

    public static PointTransaction createUsePoint(Long memberId, PointUseReqDto reqDto, Long amountToUse, Long usedPointId, LocalDateTime expiredDate) {
        return PointTransaction.builder()
                .memberId(memberId)
                .orderId(reqDto.getOrderId())
                .type(TransactionType.USE)
                .amount(amountToUse)
                .usedPointId(usedPointId)
                .expiredDate(expiredDate)
                .build();
    }


    public static PointTransaction createSavedPointCancel(PointTransaction savedTransaction) {
        return PointTransaction.builder()
                .memberId(savedTransaction.memberId)
                .orderId(savedTransaction.getOrderId())
                .type(TransactionType.SAVE_CANCEL)
                .amount(savedTransaction.getAmount())
                .originalPointId(savedTransaction.getId())
                .build();
    }

    public static PointTransaction createUsedPointCancel(PointTransaction usedTransaction, Long amount, TransactionType type, LocalDateTime expiredDate) {
        return PointTransaction.builder()
                .memberId(usedTransaction.memberId)
                .orderId(usedTransaction.getOrderId())
                .type(type) // 만료일 지난 경우 적립으로 처리
                .amount(amount)
                .cancelPointId(usedTransaction.getId())
                .expiredDate(expiredDate)
                .build();
    }
}
