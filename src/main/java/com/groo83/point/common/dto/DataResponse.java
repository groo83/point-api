package com.groo83.point.common.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class DataResponse<T> extends BaseResDto {

    private final T data;

    private DataResponse(T data) {
        super();
        this.data = data;
    }

    private DataResponse(T data, String message) {
        super(message);
        this.data = data;
    }

    public static <T> DataResponse<T> create(T data) {
        return new DataResponse<>(data);
    }

    public static <T> DataResponse<T> create(T data, String message) {
        return new DataResponse<>(data, message);
    }

    public static <T> DataResponse<T> empty() {
        return new DataResponse<>(null);
    }

}
