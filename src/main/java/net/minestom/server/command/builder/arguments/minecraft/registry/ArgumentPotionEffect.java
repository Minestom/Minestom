package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument giving a {@link PotionEffect}.
 */
public class ArgumentPotionEffect extends ArgumentRegistry<PotionEffect> {

    public ArgumentPotionEffect(String id) {
        super(id);
    }

    @Override
    public String parser() {
        return "minecraft:mob_effect";
    }

    @Override
    public PotionEffect getRegistry(@NotNull String value) {
        return PotionEffect.fromNamespaceId(value);
    }

    @Override
    public String toString() {
        return String.format("Potion<%s>", getId());
    }
}
