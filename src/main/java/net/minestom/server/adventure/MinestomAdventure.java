package net.minestom.server.adventure;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.util.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Adventure related constants, etc.
 */
public final class MinestomAdventure {
    /**
     * A codec to convert between strings and NBT.
     */
    public static final Codec<CompoundBinaryTag, String, IOException, IOException> NBT_CODEC
            = Codec.codec(TagStringIO.get()::asCompound, TagStringIO.get()::asString);

    /**
     * If components should be automatically translated in outgoing packets.
     */
    public static boolean AUTOMATIC_COMPONENT_TRANSLATION = false;
    // todo: Need to properly add a translator interface so it can check for presence of a key for the flattener.
    public static BiFunction<Component, Locale, Component> COMPONENT_TRANSLATOR = GlobalTranslator::render;

    private static Locale defaultLocale = Locale.getDefault();

    private MinestomAdventure() {
    }

    /**
     * Gets the default locale used to translate components when no overriding locale has been provided.
     *
     * @return the default locale
     */
    public static @NotNull Locale getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * Sets the default locale used to translate components when no overriding locale has been provided.
     *
     * @param defaultLocale the new default, or {@code null} to return to {@link Locale#getDefault()}
     */
    public static void setDefaultLocale(@Nullable Locale defaultLocale) {
        MinestomAdventure.defaultLocale = Objects.requireNonNullElseGet(defaultLocale, Locale::getDefault);
    }
}
