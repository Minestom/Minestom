package net.minestom.server.adventure;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
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
     * See {@link MinestomAdventure#tagStringIO()}
     */
    private static final TagStringIO tagStringIO = TagStringIO.builder()
            .emitHeterogeneousLists(true)
            .acceptHeterogeneousLists(true)
            .build();

    /**
     * A codec to convert between strings and NBT.
     */
    public static final Codec<CompoundBinaryTag, String, IOException, IOException> NBT_CODEC
            = Codec.codec(tagStringIO::asCompound, tagStringIO::asString);

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
     * Gets the {@link TagStringIO} instance used to convert SNBT.
     * This instance should be used for all Adventure related SNBT parsing and serialization.
     * Note: This instance of the {@link TagStringIO} is configured to accept and emit heterogeneous lists
     *
     * @return the tag string IO instance
     */
    public static @NotNull TagStringIO tagStringIO() {
        return tagStringIO;
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

    public static @NotNull BinaryTagHolder wrapNbt(@NotNull BinaryTag nbt) {
        return new BinaryTagHolderImpl(nbt);
    }

    public static @NotNull BinaryTag unwrapNbt(@NotNull BinaryTagHolder holder) {
        if (holder instanceof BinaryTagHolderImpl(BinaryTag nbt))
            return nbt;
        try {
            return holder.get(MinestomAdventure.NBT_CODEC);
        } catch (IOException e) {
            throw new RuntimeException("Failed to unwrap BinaryTagHolder", e);
        }
    }
}
