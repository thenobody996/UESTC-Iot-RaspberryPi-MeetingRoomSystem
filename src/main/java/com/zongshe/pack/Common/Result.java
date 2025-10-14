package com.zongshe.pack.Common;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;


    public static <T> Result<T> ok() {
        return new Result<>(20000, "success", null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(20000, "success", data);
    }

    public static <T> Result<T> ok(T data, String message) {
        return new Result<>(20000, message, data);
    }

    public static <T> Result<T> ok(String message) {
        return new Result<>(20000, message, null);
    }

    public static <T> Result<T> fail()  {
        return new Result<>(40000, "fail", null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(40000, message, null);
    }
}
