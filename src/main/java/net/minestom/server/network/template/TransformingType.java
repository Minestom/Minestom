package net.minestom.server.network.template;

import net.minestom.server.network.NetworkBuffer;

import java.util.function.Function;

public interface TransformingType<T, S> extends NetworkBuffer.Type<S> {
    NetworkBuffer.Type<T> parent();

    Function<? super T, ? extends S> to();

    Function<? super S, ? extends T> from();
}
