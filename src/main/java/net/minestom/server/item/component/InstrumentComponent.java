package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.item.instrument.Instrument;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Holder;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.utils.Either;
import org.jspecify.annotations.Nullable;

public record InstrumentComponent(Either<Holder<Instrument>, RegistryKey<Instrument>> instrument) {
    public static final NetworkBuffer.Type<InstrumentComponent> NETWORK_TYPE = NetworkBuffer
            .Either(Instrument.NETWORK_TYPE, RegistryKey.<Instrument>uncheckedNetworkType())
            .transform(InstrumentComponent::new, InstrumentComponent::instrument);
    public static final Codec<InstrumentComponent> CODEC = Codec
            .Either(Instrument.CODEC, RegistryKey.<Instrument>uncheckedCodec())
            .transform(InstrumentComponent::new, InstrumentComponent::instrument);

    public @Nullable Instrument resolve(DynamicRegistry<Instrument> registry) {
        return switch (this.instrument) {
            case Either.Left(Holder<Instrument> holder) -> holder.resolve(registry);
            case Either.Right(RegistryKey<Instrument> reference) -> registry.get(reference);
        };
    }
}