package net.minestom.server.network.player;

import net.minestom.server.ServerFlag;
import net.minestom.server.message.ChatMessageType;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientSettings(Locale locale, byte viewDistance,
                             ChatMessageType chatMessageType, boolean chatColors,
                             byte displayedSkinParts, MainHand mainHand,
                             boolean enableTextFiltering, boolean allowServerListings,
                             @NotNull ParticleStatus particleStatus) {
    public static ClientSettings DEFAULT = new ClientSettings(
            Locale.US, (byte) ServerFlag.CHUNK_VIEW_DISTANCE,
            ChatMessageType.FULL, true,
            (byte) 0x7F, MainHand.RIGHT,
            true, true,
            ParticleStatus.ALL
    );

    private static final NetworkBuffer.Type<Locale> LOCALE_SERIALIZER = STRING.transform(
            s -> {
                final String locale = s.replace("_", "-");
                return Locale.forLanguageTag(locale);
            },
            Locale::toLanguageTag
    );

    public static final NetworkBuffer.Type<ClientSettings> NETWORK_TYPE = NetworkBufferTemplate.template(
            LOCALE_SERIALIZER, ClientSettings::locale,
            BYTE, ClientSettings::viewDistance,
            Enum(ChatMessageType.class), ClientSettings::chatMessageType,
            BOOLEAN, ClientSettings::chatColors,
            BYTE, ClientSettings::displayedSkinParts,
            Enum(MainHand.class), ClientSettings::mainHand,
            BOOLEAN, ClientSettings::enableTextFiltering,
            BOOLEAN, ClientSettings::allowServerListings,
            ParticleStatus.NETWORK_TYPE, ClientSettings::particleStatus,
            ClientSettings::new);

    public ClientSettings {
        Objects.requireNonNull(locale);
        // Clamp viewDistance to valid bounds
        viewDistance = (byte) MathUtils.clamp(viewDistance, 2, 32);
        Objects.requireNonNull(chatMessageType);
        Objects.requireNonNull(mainHand);
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

    public enum ParticleStatus {
        ALL,
        DECREASED,
        MINIMAL;

        public static final NetworkBuffer.Type<ParticleStatus> NETWORK_TYPE = Enum(ParticleStatus.class);
    }
}
