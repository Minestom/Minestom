package net.minestom.server.registry;

import net.minestom.server.codec.Transcoder;
import net.minestom.server.codec.TranscoderProxy;

import java.util.Objects;

public record RegistryTranscoder<D>(
        Transcoder<D> transcoder,
        Registries registries,
        boolean forClient,
        boolean init // True for initial load
) implements TranscoderProxy<D> {

    public RegistryTranscoder(Transcoder<D> transcoder, Registries registries) {
        this(Objects.requireNonNull(transcoder), registries, false, false);
    }

    @Override
    public Transcoder<D> delegate() {
        return transcoder;
    }

}
