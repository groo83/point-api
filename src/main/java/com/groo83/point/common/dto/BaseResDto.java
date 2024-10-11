package com.groo83.point.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class BaseResDto {

    private Integer code;
    private String message;

    protected BaseResDto() {
        this.code = 200;
        this.message = "Success";
    }

    public BaseResDto(String message) {
        this.code = 200;
        this.message = message;
    }

    public static BaseResDto ok() {
        return new BaseResDto();
    }
}