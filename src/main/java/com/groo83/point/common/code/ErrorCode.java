package com.groo83.point.common.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(400, "C001", "Invalid Input Value"),
    INVALID_INPUT_JSON_FORMAT(400, "C002", " Invalid Json Format"),
    INVALID_TYPE_VALUE(400, "C003", " Invalid Type Value"),

    POINT_SAVE_ONE_TIME_MAX_LIMIT(400,"P001", "1회 적립 가능한 한도를 초과하였습니다."),
    USER_POINT_SAVE_MAX_LIMIT(400,"P002", "사용자의 보유 가능한 포인트의 최대 한도를 초과하였습니다."),
    POINT_EXPIRED_DATE_INVALID(400,"P003", "포인트 만료일을 최소 1일 이상, 최대 5년 미만으로 설정해주세요."),
    TRANSACTION_NOT_FOUND(400,"P004", "포인트 정보가 없습니다."),
    POINT_NOT_ENOUGH(400,"P005", "사용할 수 있는 포인트가 부족합니다."),
    INVALID_SAVED_POINT(400,"P006", "해당 포인트 적립 내역을 찾을 수 없습니다. 적립된 포인트가 이미 사용되었거나 적립 취소가 되었습니다."),
    INVALID_USED_POINT(400, "P007", "해당 포인트 사용 내역을 찾을 수 없습니다."),
    USED_POINT_OVER(400,"P008","취소하려는 포인트가 사용된 포인트보다 많습니다."),

    USER_NOT_FOUND(400,"M001", "사용자 정보가 없습니다."),
    EXIST_MEMBER_ID(400,"M002", "사용자 ID가 이미 존재합니다."),
    ;

    private int status;
    private final String code;
    private final String message;


}
