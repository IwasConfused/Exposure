package io.github.mortuusars.exposure.client.snapshot;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.util.ErrorMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TaskResult<T> {
    private final @Nullable T value;
    private final @Nullable ErrorMessage errorMessage;

    private TaskResult(@NotNull T value) {
        this.value = value;
        this.errorMessage = null;
    }

    private TaskResult(@NotNull ErrorMessage errorMessage) {
        this.value = null;
        this.errorMessage = errorMessage;
    }

    public static <T> TaskResult<T> success(T value) {
        return new TaskResult<>(value);
    }

    public static <T> TaskResult<T> error(ErrorMessage errorMessage) {
        return new TaskResult<>(errorMessage);
    }

    public boolean isSuccessful() {
        return value != null;
    }

    public boolean isError() {
        return errorMessage != null;
    }

    public @NotNull T getValue() {
        Preconditions.checkState(value != null, "Called getValue on an error result. Should check with isSuccessful first.");
        return value;
    }

    public @NotNull ErrorMessage getErrorMessage() {
        Preconditions.checkState(errorMessage != null, "Called getErrorMessage on an error result. Should check with isError first.");
        return errorMessage;
    }
}
