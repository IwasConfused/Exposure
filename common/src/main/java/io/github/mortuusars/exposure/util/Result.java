package io.github.mortuusars.exposure.util;

import java.util.function.Consumer;
import java.util.function.Function;

public record Result<T>(T value) {
    public <R> Result<R> then(Function<T, R> func) {
        return new Result<>(func.apply(value));
    }

    public void thenConsume(Consumer<T> consumer) {
        consumer.accept(value);
    }
}
