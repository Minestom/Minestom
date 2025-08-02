package net.minestom.server.item.instrument;

import net.kyori.adventure.text.Component;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.validate.Check;

public record InstrumentImpl(
        SoundEvent soundEvent,
        float useDuration,
        float range,
        Component description
) implements Instrument {

    @SuppressWarnings("ConstantValue") // The builder can violate the nullability constraints
    public InstrumentImpl {
        Check.argCondition(soundEvent == null, "missing sound event");
        Check.argCondition(description == null, "missing description");
        Check.argCondition(useDuration <= 0, "use duration must be positive");
        Check.argCondition(range <= 0, "range must be positive");
    }

}
