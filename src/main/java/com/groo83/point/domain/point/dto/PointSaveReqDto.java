package com.groo83.point.domain.point.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.groo83.point.common.valid.ValidEnum;
import com.groo83.point.domain.point.enums.RewardType;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PointSaveReqDto {

    private Long orderId;

    @NotNull
    private Long amount;

    private LocalDateTime expiredDate;

    @ValidEnum(target = RewardType.class, message = "ORDER(주문) 또는 ADMINISTRATOR(관리자)만 입력해주세요.")
    private String rewardType = "ORDER";

}
