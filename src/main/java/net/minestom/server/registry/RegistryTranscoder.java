package net.minestom.server.registry;

import net.minestom.server.codec.Transcoder;
import net.minestom.server.codec.TranscoderProxy;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record RegistryTranscoder<D>(
        @NotNull Transcoder<D> transcoder,
        @NotNull Registries registries,
        boolean forClient,
        boolean init // True for initial load
) implements TranscoderProxy<D> {

    public RegistryTranscoder(@NotNull Transcoder<D> transcoder, @NotNull Registries registries) {
        this(Objects.requireNonNull(transcoder), registries, false, false);
    }

    @Override
    public @NotNull Transcoder<D> delegate() {
        return transcoder;
    }

}
