package net.minestom.server.adventure.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class to access Adventure audiences.
 */
public class Audiences implements AudienceProvider<Audience> {
    private final IterableAudienceProvider collection;
    private final Audience players, server;

    /**
     * Creates a new audiences instance. <b>Do not</b> instantiate this class, instead
     * you can obtain an instance using {@link #audiences()}.
     */
    public Audiences() {
        this.collection = new IterableAudienceProvider();
        this.players = PacketGroupingAudience.of(MinecraftServer.getConnectionManager().getOnlinePlayers());
        this.server = Audience.audience(this.players, MinecraftServer.getCommandManager().getConsoleSender());
    }

    /**
     * Short-hand method for {@link MinecraftServer#getAudiences()}.
     * @return the audiences instance
     */
    public static @NotNull Audiences audiences() {
        return MinecraftServer.getAudiences();
    }

    /**
     * Gets the {@link IterableAudienceProvider} instance.
     * @return the instance
     */
    public @NotNull IterableAudienceProvider iterable() {
        return this.collection;
    }

    @Override
    public @NotNull Audience all() {
        return Audience.audience(this.server, this.custom());
    }

    @Override
    public @NotNull Audience players() {
        return this.players;
    }

    @Override
    public @NotNull Audience players(@NotNull Predicate<Player> filter) {
        return PacketGroupingAudience.of(MinecraftServer.getConnectionManager().getOnlinePlayers().stream().filter(filter).collect(Collectors.toList()));
    }

    @Override
    public @NotNull Audience console() {
        return MinecraftServer.getCommandManager().getConsoleSender();
    }

    @Override
    public @NotNull Audience server() {
        return this.server;
    }

    @Override
    public @NotNull Audience custom() {
        return Audience.audience(this.iterable().custom());
    }

    @Override
    public @NotNull Audience custom(@NotNull Key key) {
        return Audience.audience(this.iterable().custom(key));
    }

    @Override
    public @NotNull Audience custom(@NotNull Predicate<Audience> filter) {
        return Audience.audience(this.iterable().custom(filter));
    }

    @Override
    public @NotNull Audience of(@NotNull Predicate<Audience> filter) {
        return Audience.audience(this.iterable().of(filter));
    }

    @Override
    public @NotNull AudienceRegistry registry() {
        return this.iterable().registry();
    }
}
