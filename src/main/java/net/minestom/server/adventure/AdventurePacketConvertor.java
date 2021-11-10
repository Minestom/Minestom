package net.minestom.server.adventure;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.entity.Entity;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.TickUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Utility methods to convert adventure enums to their packet values.
 */
public class AdventurePacketConvertor {
    private static final Object2IntMap<NamedTextColor> NAMED_TEXT_COLOR_ID_MAP = new Object2IntArrayMap<>(16);

    static {
        NAMED_TEXT_COLOR_ID_MAP.put(NamedTextColor.BLACK, 0);
        NAMED_TEXT_COLOR_ID_MAP.put(NamedTextColor.DARK_BLUE, 1);
        NAMED_TEXT_COLOR_ID_MAP.put(NamedTextColor.DARK_GREEN, 2);
        NAMED_TEXT_COLOR_ID_MAP.put(NamedTextColor.DARK_AQUA, 3);
        NAMED_TEXT_COLOR_ID_MAP.put(NamedTextColor.DARK_RED, 4);
        NAMED_TEXT_COLOR_ID_MAP.put(NamedTextColor.DARK_PURPLE, 5);
        NAMED_TEXT_COLOR_ID_MAP.put(NamedTextColor.GOLD, 6);
        NAMED_TEXT_COLOR_ID_MAP.put(NamedTextColor.GRAY, 7);
        NAMED_TEXT_COLOR_ID_MAP.put(NamedTextColor.DARK_GRAY, 8);
        NAMED_TEXT_COLOR_ID_MAP.put(NamedTextColor.BLUE, 9);
        NAMED_TEXT_COLOR_ID_MAP.put(NamedTextColor.GREEN, 10);
        NAMED_TEXT_COLOR_ID_MAP.put(NamedTextColor.AQUA, 11);
        NAMED_TEXT_COLOR_ID_MAP.put(NamedTextColor.RED, 12);
        NAMED_TEXT_COLOR_ID_MAP.put(NamedTextColor.LIGHT_PURPLE, 13);
        NAMED_TEXT_COLOR_ID_MAP.put(NamedTextColor.YELLOW, 14);
        NAMED_TEXT_COLOR_ID_MAP.put(NamedTextColor.WHITE, 15);
    }

    /**
     * Gets the int value of a boss bar overlay.
     *
     * @param overlay the overlay
     * @return the value
     */
    public static int getBossBarOverlayValue(@NotNull BossBar.Overlay overlay) {
        return overlay.ordinal();
    }

    /**
     * Gets the byte value of a collection of boss bar flags.
     *
     * @param flags the flags
     * @return the value
     */
    public static byte getBossBarFlagValue(@NotNull Collection<BossBar.Flag> flags) {
        byte val = 0x0;
        for (BossBar.Flag flag : flags) {
            val |= flag.ordinal();
        }
        return val;
    }

    /**
     * Gets the int value of a boss bar color.
     *
     * @param color the color
     * @return the value
     */
    public static int getBossBarColorValue(@NotNull BossBar.Color color) {
        return color.ordinal();
    }

    /**
     * Gets the int value of a sound source.
     *
     * @param source the source
     * @return the value
     */
    public static int getSoundSourceValue(@NotNull Sound.Source source) {
        return source.ordinal();
    }

    /**
     * Gets the int value from a named text color.
     *
     * @param color the color
     * @return the int value
     */
    public static int getNamedTextColorValue(@NotNull NamedTextColor color) {
        return NAMED_TEXT_COLOR_ID_MAP.getInt(color);
    }

    /**
     * Creates a sound packet from a sound and a location.
     *
     * @param sound the sound
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param z     the z coordinate
     * @return the sound packet
     */
    public static @NotNull ServerPacket createSoundPacket(@NotNull Sound sound, double x, double y, double z) {
        final SoundEvent minestomSound = SoundEvent.fromNamespaceId(sound.name().asString());
        if (minestomSound == null) {
            final NamedSoundEffectPacket packet = new NamedSoundEffectPacket();
            packet.soundName = sound.name().asString();
            packet.soundSource = sound.source();
            packet.x = (int) x;
            packet.y = (int) y;
            packet.z = (int) z;
            packet.volume = sound.volume();
            packet.pitch = sound.pitch();
            return packet;
        } else {
            final SoundEffectPacket packet = new SoundEffectPacket();
            packet.soundId = minestomSound.id();
            packet.soundSource = sound.source();
            packet.x = (int) x;
            packet.y = (int) y;
            packet.z = (int) z;
            packet.volume = sound.volume();
            packet.pitch = sound.pitch();
            return packet;
        }
    }

    /**
     * Creates a sound effect packet from a sound and an emitter.
     *
     * @param sound   the sound
     * @param emitter the emitter, must be an {@link Entity}
     * @return the sound packet
     */
    public static @NotNull ServerPacket createSoundPacket(@NotNull Sound sound, Sound.@NotNull Emitter emitter) {
        if (emitter == Sound.Emitter.self())
            throw new IllegalArgumentException("you must replace instances of Emitter.self() before calling this method");
        if (!(emitter instanceof Entity entity))
            throw new IllegalArgumentException("you can only call this method with entities");

        final SoundEvent minestomSound = SoundEvent.fromNamespaceId(sound.name().asString());

        if (minestomSound != null) {
            final EntitySoundEffectPacket packet = new EntitySoundEffectPacket();
            packet.soundId = minestomSound.id();
            packet.soundSource = sound.source();
            packet.entityId = entity.getEntityId();
            packet.volume = sound.volume();
            packet.pitch = sound.pitch();
            return packet;
        } else {
            final var pos = entity.getPosition();
            final NamedSoundEffectPacket packet = new NamedSoundEffectPacket();
            packet.soundName = sound.name().asString();
            packet.soundSource = sound.source();
            packet.x = (int) pos.x();
            packet.y = (int) pos.y();
            packet.z = (int) pos.z();
            packet.volume = sound.volume();
            packet.pitch = sound.pitch();
            return packet;
        }
    }

    /**
     * Creates an entity sound packet from an Adventure sound.
     *
     * @param sound  the sound
     * @param entity the entity the sound is coming from
     * @return the packet
     * @deprecated Use {@link #createSoundPacket(Sound, Sound.Emitter)}
     */
    @Deprecated(forRemoval = true)
    public static ServerPacket createEntitySoundPacket(@NotNull Sound sound, @NotNull Entity entity) {
        return createSoundPacket(sound, entity);
    }

    /**
     * Creates a sound stop packet from a sound stop.
     *
     * @param stop the sound stop
     * @return the sound stop packet
     */
    public static ServerPacket createSoundStopPacket(@NotNull SoundStop stop) {
        StopSoundPacket packet = new StopSoundPacket();
        packet.flags = 0x0;

        final Sound.Source source = stop.source();
        if (source != null) {
            packet.flags |= 0x1;
            packet.source = AdventurePacketConvertor.getSoundSourceValue(source);
        }

        final Key sound = stop.sound();
        if (sound != null) {
            packet.flags |= 0x2;
            packet.sound = sound.asString();
        }

        return packet;
    }

    /**
     * Creates one of the three title packets from a title part and a value.
     *
     * @param part the part
     * @param value the value
     * @param <T> the type of the part
     * @return the title packet
     */
    public static <T> @NotNull ServerPacket createTitlePartPacket(@NotNull TitlePart<T> part, @NotNull T value) {
        if (part == TitlePart.TITLE) {
            return new SetTitleTextPacket((Component) value);
        } else if (part == TitlePart.SUBTITLE) {
            return new SetTitleSubTitlePacket((Component) value);
        } else if (part == TitlePart.TIMES) {
            Title.Times times = (Title.Times) value;
            return new SetTitleTimePacket(
                    TickUtils.fromDuration(times.fadeIn(), TickUtils.CLIENT_TICK_MS),
                    TickUtils.fromDuration(times.stay(), TickUtils.CLIENT_TICK_MS),
                    TickUtils.fromDuration(times.fadeOut(), TickUtils.CLIENT_TICK_MS));
        } else {
            throw new IllegalArgumentException("Unknown TitlePart " + part);
        }
    }
}
