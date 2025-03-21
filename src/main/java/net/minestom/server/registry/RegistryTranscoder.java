package net.minestom.server.registry;

import net.minestom.server.codec.Transcoder;
import net.minestom.server.codec.TranscoderProxy;
import org.jetbrains.annotations.NotNull;

public record RegistryTranscoder<D>(
        @NotNull Transcoder<D> transcoder,
        @NotNull Registries registries
) implements TranscoderProxy<D> {

    @Override
    public @NotNull Transcoder<D> delegate() {
        return transcoder;
    }

}
