package net.minestom.server.item.instrument;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.Registry;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record InstrumentImpl(
        @NotNull SoundEvent soundEvent,
        float useDuration,
        float range,
        @NotNull Component description,
        @Nullable Registry.InstrumentEntry registry
) implements Instrument {

    static final NetworkBuffer.Type<Instrument> REGISTRY_NETWORK_TYPE = NetworkBufferTemplate.template(
            SoundEvent.NETWORK_TYPE, Instrument::soundEvent,
            NetworkBuffer.FLOAT, Instrument::useDuration,
            NetworkBuffer.FLOAT, Instrument::range,
            NetworkBuffer.COMPONENT, Instrument::description,
            InstrumentImpl::new
    );
    static final BinaryTagSerializer<Instrument> REGISTRY_NBT_TYPE = BinaryTagTemplate.object(
            "sound_event", SoundEvent.NBT_TYPE, Instrument::soundEvent,
            "use_duration", BinaryTagSerializer.FLOAT, Instrument::useDuration,
            "range", BinaryTagSerializer.FLOAT, Instrument::range,
            "description", BinaryTagSerializer.NBT_COMPONENT, Instrument::description,
            InstrumentImpl::new
    );

    @SuppressWarnings("ConstantValue") // The builder can violate the nullability constraints
    public InstrumentImpl {
        Check.argCondition(soundEvent == null, "missing sound event");
        Check.argCondition(description == null, "missing description");
        Check.argCondition(useDuration <= 0, "use duration must be positive");
        Check.argCondition(range <= 0, "range must be positive");
    }

    public InstrumentImpl(
            @NotNull SoundEvent soundEvent,
            float useDuration,
            float range,
            @NotNull Component description
    ) {
        this(soundEvent, useDuration, range, description, null);
    }

    public InstrumentImpl(@NotNull Registry.InstrumentEntry registry) {
        this(registry.soundEvent(), registry.useDuration(), registry.range(), registry.description(), registry);
    }

}
