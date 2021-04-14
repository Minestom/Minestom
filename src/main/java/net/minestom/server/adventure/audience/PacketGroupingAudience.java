package net.minestom.server.adventure.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.ChatMessagePacket;
import net.minestom.server.network.packet.server.play.PlayerListHeaderAndFooterPacket;
import net.minestom.server.network.packet.server.play.TitlePacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * An audience implementation that sends grouped packets if possible.
 */
public interface PacketGroupingAudience extends ForwardingAudience {

    /**
     * Creates a packet grouping audience that copies an iterable of players. The
     * underlying collection is not copied, so changes to the collection will be
     * reflected in the audience.
     *
     * @param players the players
     * @return the audience
     */
    static PacketGroupingAudience of(Collection<Player> players) {
        return () -> players;
    }

    /**
     * Gets an iterable of the players this audience contains.
     *
     * @return the connections
     */
    @NotNull Collection<Player> getPlayers();

    @Override
    default void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        PacketUtils.sendGroupedPacket(this.getPlayers(), new ChatMessagePacket(message, ChatMessagePacket.Position.fromMessageType(type), source.uuid()));
    }

    @Override
    default void sendActionBar(@NotNull Component message) {
        PacketUtils.sendGroupedPacket(this.getPlayers(), new TitlePacket(TitlePacket.Action.SET_ACTION_BAR, message));
    }

    @Override
    default void sendPlayerListHeaderAndFooter(@NotNull Component header, @NotNull Component footer) {
        PacketUtils.sendGroupedPacket(this.getPlayers(), new PlayerListHeaderAndFooterPacket(header, footer));
    }

    @Override
    default void showTitle(@NotNull Title title) {
        PacketUtils.sendGroupedPacket(this.getPlayers(), new TitlePacket(TitlePacket.Action.SET_TITLE, title.title()));
        PacketUtils.sendGroupedPacket(this.getPlayers(), new TitlePacket(TitlePacket.Action.SET_SUBTITLE, title.subtitle()));
    }

    @Override
    default void clearTitle() {
        PacketUtils.sendGroupedPacket(this.getPlayers(), new TitlePacket(TitlePacket.Action.HIDE));
    }

    @Override
    default void resetTitle() {
        PacketUtils.sendGroupedPacket(this.getPlayers(), new TitlePacket(TitlePacket.Action.RESET));
    }

    @Override
    default void showBossBar(@NotNull BossBar bar) {
        MinecraftServer.getBossBarManager().addBossBar(this.getPlayers(), bar);
    }

    @Override
    default void hideBossBar(@NotNull BossBar bar) {
        MinecraftServer.getBossBarManager().removeBossBar(this.getPlayers(), bar);
    }

    @Override
    default void playSound(@NotNull Sound sound, double x, double y, double z) {
        PacketUtils.sendGroupedPacket(this.getPlayers(), AdventurePacketConvertor.createSoundPacket(sound, x, y, z));
    }

    @Override
    default void stopSound(@NotNull SoundStop stop) {
        PacketUtils.sendGroupedPacket(this.getPlayers(), AdventurePacketConvertor.createSoundStopPacket(stop));
    }

    @Override
    default @NotNull Iterable<? extends Audience> audiences() {
        return this.getPlayers();
    }
}
