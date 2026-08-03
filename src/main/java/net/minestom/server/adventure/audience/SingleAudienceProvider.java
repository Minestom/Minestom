package net.minestom.server.adventure.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;

import java.util.function.Predicate;

/**
 * A provider of audiences. For complex returns, this instance is backed by
 * {@link IterableAudienceProvider}.
 */
value class SingleAudienceProvider implements AudienceProvider<Audience> {

    final IterableAudienceProvider collection = new IterableAudienceProvider();
    final Audience players = PacketGroupingAudience.of(MinecraftServer.getConnectionManager().getOnlinePlayers());
    final Audience server = Audience.audience(this.players, MinecraftServer.getCommandManager().getConsoleSender());

    /**
     * Gets the {@link IterableAudienceProvider} instance.
     *
     * @return the instance
     */
    public IterableAudienceProvider iterable() {
        return this.collection;
    }

    @Override
    public Audience all() {
        return Audience.audience(this.server, this.customs());
    }

    @Override
    public Audience players() {
        return this.players;
    }

    @Override
    public Audience players(Predicate<? super Player> filter) {
        return PacketGroupingAudience.of(MinecraftServer.getConnectionManager().getOnlinePlayers().stream().filter(filter).toList());
    }

    @Override
    public Audience console() {
        return MinecraftServer.getCommandManager().getConsoleSender();
    }

    @Override
    public Audience server() {
        return this.server;
    }

    @Override
    public Audience customs() {
        return Audience.audience(this.iterable().customs());
    }

    @Override
    public Audience custom(Key key) {
        return Audience.audience(this.iterable().custom(key));
    }

    @Override
    public Audience custom(Key key, Predicate<? super Audience> filter) {
        return Audience.audience(this.iterable().custom(key, filter));
    }

    @Override
    public Audience customs(Predicate<? super Audience> filter) {
        return Audience.audience(this.iterable().customs(filter));
    }

    @Override
    public Audience all(Predicate<? super Audience> filter) {
        return Audience.audience(this.iterable().all(filter));
    }

    @Override
    public AudienceRegistry registry() {
        return this.iterable().registry();
    }
}
