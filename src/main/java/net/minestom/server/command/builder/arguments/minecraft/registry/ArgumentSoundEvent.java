package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.builder.arguments.minecraft.SuggestionType;
import net.minestom.server.entity.EntityType;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument giving an {@link SoundEvent}.
 */
public class ArgumentSoundEvent extends ArgumentRegistry<SoundEvent> {

    public ArgumentSoundEvent(String id) {
        super(id);
        suggestionType = SuggestionType.AVAILABLE_SOUNDS;
    }

    @Override
    public String parser() {
        return "minecraft:resource_location";
    }

    @Override
    public SoundEvent getRegistry(@NotNull String value) {
        return SoundEvent.fromNamespaceId(value);
    }

    @Override
    public String toString() {
        return String.format("SoundEvent<%s>", getId());
    }
}
