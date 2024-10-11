package com.groo83.point.domain.point.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RewardType {

    ORDER("ORDER", "주문"),
    ADMINISTRATOR("ADMINISTRATOR", "관리자"),
    ;

    private final String code;
    private final String type;

    public static RewardType of(String gender) {
        if(gender == null) {
            throw new IllegalArgumentException();
        }

        for (RewardType g : RewardType.values()) {
            if(g.code.equals(gender)) {
                return g;
            }
        }

        throw new IllegalArgumentException("일치하는 값이 없습니다.");
    }
}
