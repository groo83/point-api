package com.groo83.point.domain.point.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionType {

    SAVE("SAVE", "적립"),
    USE("USE", "사용"),
    SAVE_CANCEL("SAVE_CANCEL", "적릷 취소"),
    USE_CANCEL("USE_CANCEL", "사용 취소"),
    ;

    private final String code;
    private final String type;
}
