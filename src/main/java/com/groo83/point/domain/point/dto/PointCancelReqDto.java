package com.groo83.point.domain.point.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PointCancelReqDto {

    @NotNull
    private Long orderId;

    private Long amount;

}
