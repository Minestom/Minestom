package net.minestom.server.adventure;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.EntitySoundEffectPacket;
import net.minestom.server.network.packet.server.play.NamedSoundEffectPacket;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.network.packet.server.play.StopSoundPacket;
import net.minestom.server.registry.Registries;
import net.minestom.server.sound.SoundEvent;
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
     * @param overlay the overlay
     * @return the value
     */
    public static int getBossBarOverlayValue(@NotNull BossBar.Overlay overlay) {
        return overlay.ordinal();
    }

    /**
     * Gets the byte value of a collection of boss bar flags.
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
     * @param color the color
     * @return the value
     */
    public static int getBossBarColorValue(@NotNull BossBar.Color color) {
        return color.ordinal();
    }

    /**
     * Gets the int value of a sound source.
     * @param source the source
     * @return the value
     */
    public static int getSoundSourceValue(@NotNull Sound.Source source) {
        return source.ordinal();
    }

    /**
     * Gets the int value from a named text color.
     * @param color the color
     * @return the int value
     */
    public static int getNamedTextColorValue(@NotNull NamedTextColor color) {
        return NAMED_TEXT_COLOR_ID_MAP.getInt(color);
    }

    /**
     * Creates a sound packet from a sound and a location.
     * @param sound the sound
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the sound packet
     */
    public static ServerPacket createSoundPacket(@NotNull Sound sound, double x, double y, double z) {
        SoundEvent minestomSound = Registries.getSoundEvent(sound.name());

        if (minestomSound == null) {
            NamedSoundEffectPacket packet = new NamedSoundEffectPacket();
            packet.soundName = sound.name().asString();
            packet.soundSource = sound.source();
            packet.x = (int) x;
            packet.y = (int) y;
            packet.z = (int) z;
            packet.volume = sound.volume();
            packet.pitch = sound.pitch();
            return packet;
        } else {
            SoundEffectPacket packet = new SoundEffectPacket();
            packet.soundId = minestomSound.getId();
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
     * Creates an entity sound packet from an Adventure sound.
     * @param sound the sound
     * @param entity the entity the sound is coming from
     * @return the packet
     */
    public static ServerPacket createEntitySoundPacket(@NotNull Sound sound, @NotNull Entity entity) {
        SoundEvent soundEvent = Registries.getSoundEvent(sound.name());

        if (soundEvent == null) {
            throw new IllegalArgumentException("Sound must be a valid sound event.");
        } else {
            EntitySoundEffectPacket packet = new EntitySoundEffectPacket();
            packet.soundId = soundEvent.getId();
            packet.soundSource = sound.source();
            packet.entityId = entity.getEntityId();
            packet.volume = sound.volume();
            packet.pitch = sound.pitch();
            return packet;
        }
    }

    /**
     * Creates a sound stop packet from a sound stop.
     * @param stop the sound stop
     * @return the sound stop packet
     */
    public static ServerPacket createSoundStopPacket(@NotNull SoundStop stop) {
        StopSoundPacket packet = new StopSoundPacket();
        packet.flags = 0x0;

        if (stop.source() != null) {
            packet.flags |= 0x1;
            packet.source = AdventurePacketConvertor.getSoundSourceValue(stop.source());
        }

        if (stop.sound() != null) {
            packet.flags |= 0x2;
            packet.sound = stop.sound().asString();
        }

        return packet;
    }
}
