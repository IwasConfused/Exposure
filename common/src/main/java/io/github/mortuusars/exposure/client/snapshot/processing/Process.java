package io.github.mortuusars.exposure.client.snapshot.processing;

public abstract class Process {
    public static Processor with(Processor processor) {
        return processor;
    }

    public static Processor with(Processor... processors) {
        Processor result = Processor.EMPTY;
        for (Processor processor : processors) {
            result = result.then(processor);
        }
        return result;
    }
}
