package com.groo83.point.domain.member;

import com.groo83.point.common.entity.BaseEntity;
import com.groo83.point.domain.member.dto.MemberPointReqDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
public class MemberPoint extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    @ColumnDefault("0L")
    private Long pointBalance; // 보유 포인트

    private Long maximumLimit;

    @Version
    private Long version; // 값이 일치하지 않으면 충돌 Exception 발생, 트랜잭션 롤백

    @Builder
    public MemberPoint(Long memberId, Long pointBalance, Long maximumLimit) {
        this.memberId = memberId;
        this.pointBalance = pointBalance;
        this.maximumLimit = maximumLimit;
    }

    public static MemberPoint toEntity(MemberPointReqDto dto) {
        return MemberPoint.builder()
                .memberId(dto.getMemberId())
                .maximumLimit(dto.getMaximumLimit())
                .build();
    }

    /**
     * 포인트 계산
     * @param points
     * 양수 - 증가
     * 음수 - 감소
     */
    public void calculateBalance(Long points) {
        // 사용자별 최대 한도 초과 검증은 서비스 레이어에서 사전 검증
        this.pointBalance += points;
    }

    public void updateMaximumLimit(Long maximumLimit) {
        this.maximumLimit = maximumLimit;
    }
}
