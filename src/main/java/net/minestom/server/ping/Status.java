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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;

public record Status(
        @NotNull Component description,
        byte @Nullable [] favicon,
        @NotNull VersionInfo versionInfo,
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

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static @NotNull Builder builder(@NotNull Status status) {
        return new Builder(status);
    }

    public record VersionInfo(@NotNull String name, int protocolVersion) {
        public static final VersionInfo DEFAULT = new VersionInfo(MinecraftServer.VERSION_NAME, MinecraftServer.PROTOCOL_VERSION);
        public static final Codec<VersionInfo> CODEC = StructCodec.struct(
                "name", Codec.STRING, VersionInfo::name,
                "protocol", Codec.INT, VersionInfo::protocolVersion,
                VersionInfo::new);
    }

    public record PlayerInfo(int onlinePlayers, int maxPlayers, @NotNull List<@NotNull NamedAndIdentified> sample) {
        private static final Codec<Component> LEGACY_CODEC = Codec.STRING.transform(
                string -> LegacyComponentSerializer.legacySection().deserialize(string),
                component -> LegacyComponentSerializer.legacySection().serialize(component));

        private static final Codec<NamedAndIdentified> SAMPLE_CODEC = StructCodec.struct(
                "name", LEGACY_CODEC, NamedAndIdentified::getName,
                "id", Codec.STRING.transform(UUID::fromString, UUID::toString), NamedAndIdentified::getUuid,
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
            this(onlinePlayers, maxPlayers, Collections.emptyList());
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public static PlayerInfo online() {
            Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
            return new PlayerInfo(players.size(), players.size() + 1, (List<NamedAndIdentified>) (List) players);
        }

        public static @NotNull Builder builder() {
            return new Builder();
        }

        public static @NotNull Builder builder(PlayerInfo playerInfo) {
            return new Builder(playerInfo);
        }

        public static final class Builder {
            private int onlinePlayers;
            private int maxPlayers;
            private @NotNull List<@NotNull NamedAndIdentified> sample;

            private Builder() {
                this.sample = new ArrayList<>();
            }

            private Builder(@NotNull PlayerInfo playerInfo) {
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
            public Builder sample(@NotNull List<@NotNull NamedAndIdentified> sample) {
                this.sample = sample;
                return this;
            }

            @Contract(value = "_ -> this")
            public Builder sample(@NotNull NamedAndIdentified profile) {
                this.sample.add(profile);
                return this;
            }

            @Contract(value = "_ -> this")
            public Builder sample(@NotNull GameProfile profile) {
                return this.sample(NamedAndIdentified.of(profile.name(), profile.uuid()));
            }

            @Contract(value = "_ -> this")
            public Builder sample(@NotNull Component component) {
                return this.sample(NamedAndIdentified.named(component));
            }

            @Contract(value = "_ -> this")
            public Builder sample(@NotNull String string) {
                return this.sample(NamedAndIdentified.named(string));
            }

            public @NotNull PlayerInfo build() {
                return new PlayerInfo(this.onlinePlayers, this.maxPlayers, this.sample);
            }
        }
    }

    public static final class Builder {
        public static final Component DEFAULT_DESCRIPTION = Component.text("Minestom Server");

        private @NotNull Component description;
        private byte @Nullable [] favicon;
        private @NotNull VersionInfo versionInfo;
        private @Nullable PlayerInfo playerInfo;
        private boolean enforcesSecureChat;

        private Builder() {
            this.description = DEFAULT_DESCRIPTION;
            this.versionInfo = VersionInfo.DEFAULT;
            this.playerInfo = PlayerInfo.online();
        }

        private Builder(Status status) {
            this.description = status.description;
            this.favicon = status.favicon;
            this.versionInfo = status.versionInfo;
            this.playerInfo = status.playerInfo;
            this.enforcesSecureChat = status.enforcesSecureChat;
        }

        @Contract(value = "_ -> this")
        public Builder description(@NotNull Component description) {
            this.description = description;
            return this;
        }

        @Contract(value = "_ -> this")
        public @NotNull Builder favicon(byte @Nullable [] favicon) {
            this.favicon = favicon;
            return this;
        }

        @Contract(value = "_ -> this")
        public @NotNull Builder versionInfo(@NotNull VersionInfo versionInfo) {
            this.versionInfo = versionInfo;
            return this;
        }

        @Contract(value = "_ -> this")
        public @NotNull Builder playerInfo(@Nullable PlayerInfo playerInfo) {
            this.playerInfo = playerInfo;
            return this;
        }

        @Contract(value = "_, _ -> this")
        public @NotNull Builder playerInfo(int onlinePlayers, int maxPlayers) {
            this.playerInfo = new PlayerInfo(onlinePlayers, maxPlayers);
            return this;
        }

        @Contract(value = "_ -> this")
        public @NotNull Builder enforcesSecureChat(boolean enforcesSecureChat) {
            this.enforcesSecureChat = enforcesSecureChat;
            return this;
        }

        public @NotNull Status build() {
            return new Status(
                    this.description,
                    this.favicon,
                    this.versionInfo,
                    this.playerInfo,
                    this.enforcesSecureChat);
        }
    }
}
