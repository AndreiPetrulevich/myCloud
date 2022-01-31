package geekbrains.myCloud.core;

import java.util.Optional;

// Optional -> ifPresent ... throws

public final class Rethrow<T> {
    private Optional<T> optional;

    public Rethrow(Optional<T> optional) {
        this.optional = optional;
    }

    public static <T> Rethrow<T> of(Optional<T> optional) {
        return new Rethrow<>(optional);
    }

    public <E extends Exception> void ifPresent(ThrowingConsumer<T, E> consumer) throws E {
        if (optional.isPresent()) {
            consumer.accept(optional.get());
        }
    }
}