package net.minestom.server.entity;

import net.minestom.server.ServerFlag;
import net.minestom.server.message.ChatMessageType;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.MathUtils;

import static net.minestom.server.network.NetworkBuffer.*;

public record PlayerSettings(String locale, byte viewDistance,
                             ChatMessageType chatMessageType, boolean chatColors,
                             byte displayedSkinParts, MainHand mainHand,
                             boolean enableTextFiltering, boolean allowServerListings) {
    public static PlayerSettings DEFAULT = new PlayerSettings(
            "en_US", (byte) ServerFlag.CHUNK_VIEW_DISTANCE,
            ChatMessageType.FULL, true,
            (byte) 0x7F, MainHand.RIGHT,
            true, true
    );

    public static final NetworkBuffer.Type<PlayerSettings> NETWORK_TYPE = NetworkBufferTemplate.template(
            STRING, PlayerSettings::locale,
            BYTE, PlayerSettings::viewDistance,
            Enum(ChatMessageType.class), PlayerSettings::chatMessageType,
            BOOLEAN, PlayerSettings::chatColors,
            BYTE, PlayerSettings::displayedSkinParts,
            Enum(MainHand.class), PlayerSettings::mainHand,
            BOOLEAN, PlayerSettings::enableTextFiltering,
            BOOLEAN, PlayerSettings::allowServerListings,
            PlayerSettings::new);

    public PlayerSettings {
        // Clamp viewDistance to valid bounds
        viewDistance = (byte) MathUtils.clamp(viewDistance, 2, 32);
    }

    public int effectiveViewDistance() {
        return Math.min(viewDistance(), ServerFlag.CHUNK_VIEW_DISTANCE);
    }

    /**
     * Represents where is located the main hand of the player (can be changed in Minecraft option).
     */
    public enum MainHand {
        LEFT,
        RIGHT
    }
}
