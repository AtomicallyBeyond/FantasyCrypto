package com.digitalartsplayground.fantasycrypto.util;

import androidx.annotation.NonNull;

public class Resource<T> {

    @NonNull public final Status status;
    @NonNull public final T data;
    @NonNull public final String message;

    private Resource(@NonNull Status status, @NonNull T data, @NonNull String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> success(@NonNull T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(String msg, T data) {
        return new Resource<>(Status.ERROR, data, msg);
    }

    public static <T> Resource<T> loading(T data) {
        return new Resource<>(Status.LOADING, data, null);
    }

    public enum Status {SUCCESS, ERROR, LOADING}
}
