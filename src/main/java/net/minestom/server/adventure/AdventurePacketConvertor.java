package net.minestom.server.adventure;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.entity.Entity;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.TickUtils;
import net.minestom.server.utils.collection.ObjectArray;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility methods to convert adventure enums to their packet values.
 */
public final class AdventurePacketConvertor {
    private static final Map<NamedTextColor, Integer> NAMED_TEXT_COLOR_ID_MAP;
    private static final List<NamedTextColor> ID_NAMED_TEXT_COLOR_MAP;

    static {
        Object2IntArrayMap<NamedTextColor> COLOR_ID_MAP = new Object2IntArrayMap<>(16);
        COLOR_ID_MAP.put(NamedTextColor.BLACK, 0);
        COLOR_ID_MAP.put(NamedTextColor.DARK_BLUE, 1);
        COLOR_ID_MAP.put(NamedTextColor.DARK_GREEN, 2);
        COLOR_ID_MAP.put(NamedTextColor.DARK_AQUA, 3);
        COLOR_ID_MAP.put(NamedTextColor.DARK_RED, 4);
        COLOR_ID_MAP.put(NamedTextColor.DARK_PURPLE, 5);
        COLOR_ID_MAP.put(NamedTextColor.GOLD, 6);
        COLOR_ID_MAP.put(NamedTextColor.GRAY, 7);
        COLOR_ID_MAP.put(NamedTextColor.DARK_GRAY, 8);
        COLOR_ID_MAP.put(NamedTextColor.BLUE, 9);
        COLOR_ID_MAP.put(NamedTextColor.GREEN, 10);
        COLOR_ID_MAP.put(NamedTextColor.AQUA, 11);
        COLOR_ID_MAP.put(NamedTextColor.RED, 12);
        COLOR_ID_MAP.put(NamedTextColor.LIGHT_PURPLE, 13);
        COLOR_ID_MAP.put(NamedTextColor.YELLOW, 14);
        COLOR_ID_MAP.put(NamedTextColor.WHITE, 15);

        ObjectArray<NamedTextColor> array = ObjectArray.singleThread(16);
        COLOR_ID_MAP.forEach((key, value) -> array.set(value, key));

        // We are only using these to ensure they are never modified at runtime, and use constructs encapsulated by the JVM.
        NAMED_TEXT_COLOR_ID_MAP = Map.copyOf(COLOR_ID_MAP);
        ID_NAMED_TEXT_COLOR_MAP = array.toList();
    }

    @ApiStatus.Experimental
    public static final NetworkBuffer.Type<NamedTextColor> NAMED_TEXT_COLOR = NetworkBuffer.VAR_INT
            .transform(AdventurePacketConvertor::getNamedTextColor, AdventurePacketConvertor::getNamedTextColorValue);

    @ApiStatus.Experimental
    public static final NetworkBuffer.Type<Sound.Source> SOUND_SOURCE_TYPE = NetworkBuffer.Enum(Sound.Source.class);

    /**
     * Gets the int value of a boss bar overlay.
     *
     * @param overlay the overlay
     * @return the value
     */
    public static int getBossBarOverlayValue(BossBar.Overlay overlay) {
        return overlay.ordinal();
    }

    /**
     * Gets the byte value of a collection of boss bar flags.
     *
     * @param flags the flags
     * @return the value
     */
    public static byte getBossBarFlagValue(Collection<BossBar.Flag> flags) {
        byte val = 0x0;
        for (BossBar.Flag flag : flags) {
            val |= (byte) (1 << flag.ordinal());
        }
        return val;
    }

    /**
     * Gets the int value of a boss bar color.
     *
     * @param color the color
     * @return the value
     */
    public static int getBossBarColorValue(BossBar.Color color) {
        return color.ordinal();
    }

    /**
     * Gets the int value of a sound source.
     *
     * @param source the source
     * @return the value
     */
    public static int getSoundSourceValue(Sound.Source source) {
        return source.ordinal();
    }

    /**
     * Gets the int value from a named text color.
     *
     * @param color the color
     * @return the int value
     */
    public static int getNamedTextColorValue(NamedTextColor color) {
        return NAMED_TEXT_COLOR_ID_MAP.get(color);
    }

    /**
     * Gets the named text color from the int value, see {@link #getNamedTextColorValue(NamedTextColor)}.
     *
     * @param id the color value
     * @return the int value
     */
    public static NamedTextColor getNamedTextColor(int id) {
        return ID_NAMED_TEXT_COLOR_MAP.get(id);
    }

    /**
     * Creates a sound packet from a sound and a location.<br>
     * Random variation by default unless a seed is provided in the {@link Sound}.
     *
     * @param sound the sound
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param z     the z coordinate
     * @return the sound packet
     */
    public static ServerPacket createSoundPacket(Sound sound, double x, double y, double z) {
        SoundEvent minestomSound = SoundEvent.fromKey(sound.name());
        if (minestomSound == null) minestomSound = SoundEvent.of(sound.name(), null);

        final long seed = sound.seed().orElse(ThreadLocalRandom.current().nextLong());
        return new SoundEffectPacket(minestomSound, sound.source(), (int) x, (int) y, (int) z, sound.volume(), sound.pitch(), seed);
    }

    /**
     * Creates a sound effect packet from a sound and an emitter.<br>
     * Random variation by default unless a seed is provided in the {@link Sound}.
     *
     * @param sound   the sound
     * @param emitter the emitter, must be an {@link Entity}
     * @return the sound packet
     */
    public static ServerPacket createSoundPacket(Sound sound, Sound.Emitter emitter) {
        if (emitter == Sound.Emitter.self())
            throw new IllegalArgumentException("you must replace instances of Emitter.self() before calling this method");
        if (!(emitter instanceof Entity entity))
            throw new IllegalArgumentException("you can only call this method with entities");

        SoundEvent minestomSound = SoundEvent.fromKey(sound.name());
        if (minestomSound == null) minestomSound = SoundEvent.of(sound.name(), null);

        final long seed = sound.seed().orElse(ThreadLocalRandom.current().nextLong());
        return new EntitySoundEffectPacket(minestomSound, sound.source(), entity.getEntityId(), sound.volume(), sound.pitch(), seed);
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
    public static ServerPacket createEntitySoundPacket(Sound sound, Entity entity) {
        return createSoundPacket(sound, entity);
    }

    /**
     * Creates a sound stop packet from a sound stop.
     *
     * @param stop the sound stop
     * @return the sound stop packet
     */
    public static ServerPacket createSoundStopPacket(SoundStop stop) {
        Sound.Source source = stop.source();
        final String sound;

        final Key soundKey = stop.sound();
        if (soundKey != null) {
            sound = soundKey.asString();
        } else {
            sound = null;
        }

        if (source != null && sound != null) {
            return new StopSoundPacket(new StopSoundPacket.SourceAndSound(source, sound));
        } else if (source != null) {
            return new StopSoundPacket(new StopSoundPacket.Source(source));
        } else if (sound != null) {
            return new StopSoundPacket(new StopSoundPacket.Sound(sound));
        } else {
            return new StopSoundPacket(new StopSoundPacket.All());
        }
    }

    /**
     * Creates one of the three title packets from a title part and a value.
     *
     * @param part  the part
     * @param value the value
     * @param <T>   the type of the part
     * @return the title packet
     */
    public static <T> ServerPacket createTitlePartPacket(TitlePart<T> part, T value) {
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
