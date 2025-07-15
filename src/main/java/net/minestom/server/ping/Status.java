package net.minestom.server.ping;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.Player;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.utils.identity.NamedAndIdentified;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

public record Status(
        Component description,
        byte @Nullable [] favicon,
        VersionInfo versionInfo,
        @Nullable PlayerInfo playerInfo,
        boolean enforcesSecureChat
) {
    private static final String FAVICON_PREFIX = "data:image/png;base64,";

    public static final Codec<byte @Nullable []> FAVICON_CODEC = Codec.STRING.transform(
            string -> {
                Check.argCondition(!string.startsWith(FAVICON_PREFIX), "Favicon format must be a PNG image encoded in base 64!");
                return Base64.getDecoder().decode(string.substring(FAVICON_PREFIX.length()).getBytes(StandardCharsets.UTF_8));
            }, data -> FAVICON_PREFIX + new String(Base64.getEncoder().encode(data), StandardCharsets.UTF_8));

    public static final Codec<Status> CODEC = StructCodec.struct(
            "description", Codec.COMPONENT.optional(Component.empty()), Status::description,
            "favicon", FAVICON_CODEC.optional(), Status::favicon,
            "version", VersionInfo.CODEC, Status::versionInfo,
            "players", PlayerInfo.CODEC.optional(), Status::playerInfo,
            "enforcesSecureChat", Codec.BOOLEAN.optional(false), Status::enforcesSecureChat,
            Status::new);

    public Status {
        if (favicon != null) {
            favicon = favicon.clone();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Status status) {
        return new Builder(status);
    }

    public record VersionInfo(String name, int protocolVersion) {
        public static final VersionInfo DEFAULT = new VersionInfo(MinecraftServer.VERSION_NAME, MinecraftServer.PROTOCOL_VERSION);
        public static final Codec<VersionInfo> CODEC = StructCodec.struct(
                "name", Codec.STRING, VersionInfo::name,
                "protocol", Codec.INT, VersionInfo::protocolVersion,
                VersionInfo::new);
    }

    public record PlayerInfo(int onlinePlayers, int maxPlayers, List<NamedAndIdentified> sample) {
        private static final Codec<Component> LEGACY_CODEC = Codec.STRING.transform(
                string -> LegacyComponentSerializer.legacySection().deserialize(string),
                component -> LegacyComponentSerializer.legacySection().serialize(component));

        private static final Codec<NamedAndIdentified> SAMPLE_CODEC = StructCodec.struct(
                "name", LEGACY_CODEC, NamedAndIdentified::getName,
                "id", Codec.UUID_STRING, NamedAndIdentified::getUuid,
                NamedAndIdentified::of);

        public static final Codec<PlayerInfo> CODEC = StructCodec.struct(
                "online", Codec.INT, PlayerInfo::onlinePlayers,
                "max", Codec.INT, PlayerInfo::maxPlayers,
                "sample", SAMPLE_CODEC.list(), PlayerInfo::sample,
                PlayerInfo::new);

        public PlayerInfo {
            sample = List.copyOf(sample);
        }

        public PlayerInfo(int onlinePlayers, int maxPlayers) {
            this(onlinePlayers, maxPlayers, List.of());
        }

        public static PlayerInfo onlineCount() {
            final Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
            return new PlayerInfo(players.size(), players.size() + 1, List.of());
        }

        /**
         * @param maxSamples The maximum number of player entries to include in the sample
         * @return A {@link PlayerInfo} containing the online count, and a sample of online players.
         */
        public static PlayerInfo online(int maxSamples) {
            final Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
            final List<NamedAndIdentified> samples = new ArrayList<>(Math.min(maxSamples, players.size()));
            for (final Player player : players) {
                if (!player.getSettings().allowServerListings())
                    continue;
                samples.add(player);
                if (samples.size() >= maxSamples)
                    break;
            }
            return new PlayerInfo(players.size(), players.size() + 1, samples);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static Builder builder(PlayerInfo playerInfo) {
            return new Builder(playerInfo);
        }

        public static final class Builder {
            private int onlinePlayers;
            private int maxPlayers;
            private List<NamedAndIdentified> sample;

            private Builder() {
                this.sample = new ArrayList<>();
            }

            private Builder(PlayerInfo playerInfo) {
                this.onlinePlayers = playerInfo.onlinePlayers;
                this.maxPlayers = playerInfo.maxPlayers;
                this.sample = new ArrayList<>(playerInfo.sample);
            }

            @Contract(value = "_ -> this")
            public Builder onlinePlayers(int onlinePlayers) {
                this.onlinePlayers = onlinePlayers;
                return this;
            }

            @Contract(value = "_ -> this")
            public Builder maxPlayers(int maxPlayers) {
                this.maxPlayers = maxPlayers;
                return this;
            }

            @Contract(value = "_ -> this")
            public Builder sample(List<NamedAndIdentified> sample) {
                this.sample = sample;
                return this;
            }

            @Contract(value = "_ -> this")
            public Builder sample(NamedAndIdentified profile) {
                this.sample.add(profile);
                return this;
            }

            @Contract(value = "_ -> this")
            public Builder sample(GameProfile profile) {
                return this.sample(NamedAndIdentified.of(profile.name(), profile.uuid()));
            }

            @Contract(value = "_ -> this")
            public Builder sample(Component component) {
                return this.sample(NamedAndIdentified.named(component));
            }

            @Contract(value = "_ -> this")
            public Builder sample(String string) {
                return this.sample(NamedAndIdentified.named(string));
            }

            public PlayerInfo build() {
                return new PlayerInfo(this.onlinePlayers, this.maxPlayers, this.sample);
            }
        }
    }

    public static final class Builder {
        public static final Component DEFAULT_DESCRIPTION = Component.text("Minestom Server");

        private Component description;
        private byte @Nullable [] favicon;
        private VersionInfo versionInfo;
        private @Nullable PlayerInfo playerInfo;
        private boolean enforcesSecureChat;

        private Builder() {
            this.description = DEFAULT_DESCRIPTION;
            this.versionInfo = VersionInfo.DEFAULT;
            this.playerInfo = PlayerInfo.onlineCount();
        }

        private Builder(Status status) {
            this.description = status.description;
            this.favicon = status.favicon;
            this.versionInfo = status.versionInfo;
            this.playerInfo = status.playerInfo;
            this.enforcesSecureChat = status.enforcesSecureChat;
        }

        @Contract(value = "_ -> this")
        public Builder description(Component description) {
            this.description = description;
            return this;
        }

        @Contract(value = "_ -> this")
        public Builder favicon(byte @Nullable [] favicon) {
            this.favicon = favicon;
            return this;
        }

        @Contract(value = "_ -> this")
        public Builder versionInfo(VersionInfo versionInfo) {
            this.versionInfo = versionInfo;
            return this;
        }

        @Contract(value = "_ -> this")
        public Builder playerInfo(@Nullable PlayerInfo playerInfo) {
            this.playerInfo = playerInfo;
            return this;
        }

        @Contract(value = "_, _ -> this")
        public Builder playerInfo(int onlinePlayers, int maxPlayers) {
            this.playerInfo = new PlayerInfo(onlinePlayers, maxPlayers);
            return this;
        }

        @Contract(value = "_ -> this")
        public Builder enforcesSecureChat(boolean enforcesSecureChat) {
            this.enforcesSecureChat = enforcesSecureChat;
            return this;
        }

        public Status build() {
            return new Status(
                    this.description,
                    this.favicon,
                    this.versionInfo,
                    this.playerInfo,
                    this.enforcesSecureChat);
        }
    }
}
