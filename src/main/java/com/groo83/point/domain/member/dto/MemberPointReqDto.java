package com.groo83.point.domain.member.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberPointReqDto {

    private Long memberId;

    private Long maximumLimit; // 최대 보유 한도

}
