package io.github.mortuusars.exposure.client.image.processor;

import io.github.mortuusars.exposure.client.image.Image;

import java.util.function.Function;

public interface Process {
    static Function<Image, Image> with(Processor processor) {
        return processor::process;
    }

    static Function<Image, Image> with(Processor... processors) {
        Processor result = Processor.EMPTY;
        for (Processor processor : processors) {
            result = result.then(processor);
        }
        return result::process;
    }
}
