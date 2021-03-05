package net.minestom.server.adventure;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility methods to convert adventure enums to their packet values.
 */
public class AdventurePacketConvertor {

    public static int getBossBarOverlayValue(@NotNull BossBar.Overlay overlay) {
        return overlay.ordinal();
    }

    public static byte getBossBarFlagValue(@NotNull Collection<BossBar.Flag> flags) {
        byte val = 0x0;
        for (BossBar.Flag flag : flags) {
            val |= flag.ordinal();
        }
        return val;
    }

    public static int getBossBarColorValue(@NotNull BossBar.Color color) {
        return color.ordinal();
    }

    public static int getSoundSourceValue(@NotNull Sound.Source source) {
        return source.ordinal();
    }

    public static byte getMessageTypeValue(@NotNull MessageType messageType) {
        switch (messageType) {
            case CHAT: return 0x00;
            case SYSTEM: return 0x01;
        }

        throw new IllegalArgumentException("Cannot get message type");
    }
}
