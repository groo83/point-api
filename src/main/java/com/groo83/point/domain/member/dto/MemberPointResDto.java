package com.groo83.point.domain.member.dto;

import com.groo83.point.domain.member.MemberPoint;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberPointResDto {

    private Long memberId;

    private Long pointBalance; // 보유 포인트

    private Long maximumLimit; // 최대 보유 한도

    public static MemberPointResDto fromEntity(MemberPoint member) {
        return MemberPointResDto.builder()
                .memberId(member.getMemberId())
                .pointBalance(member.getPointBalance())
                .maximumLimit(member.getMaximumLimit())
                .build();
    }
}
