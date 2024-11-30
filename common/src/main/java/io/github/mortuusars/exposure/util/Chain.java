package io.github.mortuusars.exposure.util;

import java.util.function.Function;

public class Chain<T> {
    private final T value;

    public Chain(T value) {
        this.value = value;
    }

    public static <T> Chain<T> start(T value) {
        return new Chain<>(value);
    }

    public <R> Result<R> apply(Function<T, R> func) {
        return new Result<>(func.apply(value));
    }
}
