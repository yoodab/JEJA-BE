package com.jeja.jejabe.global.response;

import lombok.Getter;

@Getter
public class ApiResponseForm<T> {
    private final String status;
    private final String code;
    private final String message;
    private final T data;

    public ApiResponseForm(String status, String code, String message, T data) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponseForm<T> success(T data, String message) {
        return new ApiResponseForm<>("success", "200", message, data);
    }

    public static <T> ApiResponseForm<T> success(T data) {
        return new ApiResponseForm<>("success", "200", "OK", data);
    }

    public static ApiResponseForm<Void> success(String message) {
        return new ApiResponseForm<>("success", "200", message, null);
    }

    public static <T> ApiResponseForm<T> error(String code, String message) {
        return new ApiResponseForm<>("error", code, message, null);
    }
}
