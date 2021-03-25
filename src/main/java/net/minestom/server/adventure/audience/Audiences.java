package net.minestom.server.adventure.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Utility class to access Adventure audiences.
 */
public class Audiences implements AudienceProvider<Audience> {
    private final CollectionAudienceProvider collection = new CollectionAudienceProvider();

    /**
     * Short-hand method for {@link MinecraftServer#getAudiences()}.
     * @return the audiences instance
     */
    public static @NotNull Audiences audiences() {
        return MinecraftServer.getAudiences();
    }

    /**
     * Gets the {@link CollectionAudienceProvider} instance.
     * @return the instance
     */
    public @NotNull CollectionAudienceProvider collection() {
        return this.collection;
    }

    @Override
    public @NotNull Audience all() {
        return Audience.audience(this.players(), this.console(), this.custom());
    }

    @Override
    public @NotNull Audience players() {
        return PacketGroupingAudience.of(this.collection().players());
    }

    @Override
    public @NotNull Audience players(@NotNull Predicate<Player> filter) {
        return PacketGroupingAudience.of(this.collection().players(filter));
    }

    @Override
    public @NotNull Audience console() {
        return MinecraftServer.getCommandManager().getConsoleSender();
    }

    @Override
    public @NotNull Audience server() {
        return Audience.audience(this.players(), this.console());
    }

    @Override
    public @NotNull Audience custom() {
        return Audience.audience(this.collection().custom());
    }

    @Override
    public @NotNull Audience custom(@NotNull Key key) {
        return Audience.audience(this.collection().custom(key));
    }

    @Override
    public @NotNull Audience custom(@NotNull Predicate<Audience> filter) {
        return Audience.audience(this.collection().custom(filter));
    }

    @Override
    public @NotNull Audience of(@NotNull Predicate<Audience> filter) {
        return Audience.audience(this.collection().of(filter));
    }
}
