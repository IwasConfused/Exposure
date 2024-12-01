package io.github.mortuusars.exposure.client.snapshot;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.util.Chain;
import io.github.mortuusars.exposure.util.ErrorMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SnapShot {
    protected final Captor captor;
    protected final List<Consumer<Image>> captureConsumers;
    protected final List<Consumer<ErrorMessage>> captureErrorConsumers;

    protected boolean started;
    protected boolean done;

    public SnapShot(Captor captor, List<Consumer<Image>> captureConsumers, List<Consumer<ErrorMessage>> captureErrorConsumers) {
        this.captor = captor;
        this.captureConsumers = captureConsumers;
        this.captureErrorConsumers = captureErrorConsumers;
    }

    public void start() {
        started = true;
        captor.capture()
                .thenAccept(result -> {
                    done = true;

                    Exposure.LOGGER.info("Thread: {}", Thread.currentThread().getName());

                    if (result.isSuccessful()) {
                        Image image = result.getImage();
                        captureConsumers.forEach(consumer -> consumer.accept(image));
                        image.close();
                    }
                    if (result.isError()) {
                        captureErrorConsumers.forEach(consumer -> consumer.accept(result.getErrorMessage()));
                    }
                });
    }

    public void tick() {
        if (done) {
            return;
        }

        captor.frameTick();
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isDone() {
        return done;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private Captor captor;
        private final List<Consumer<Image>> consumers = new ArrayList<>();
        private final List<Consumer<ErrorMessage>> errorConsumers = new ArrayList<>();

        public Builder captureWith(Captor captor) {
            this.captor = captor;
            return this;
        }

        public Builder then(Consumer<Chain<Image>> consumer) {
            consumers.add(image -> consumer.accept(Chain.start(image)));
            return this;
        }

        public Builder onCaptureError(Consumer<ErrorMessage> consumer) {
            errorConsumers.add(consumer);
            return this;
        }

        public SnapShot build() {
            Preconditions.checkState(captor != null, "Captor was not specified. Use 'captureWith' to specify.");
            return new SnapShot(captor, consumers, errorConsumers);
        }
    }
}
