package net.minestom.server.adventure.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.MinecraftServer;
import net.minestom.server.advancements.Notification;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.message.ChatPosition;
import net.minestom.server.message.Messenger;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.ActionBarPacket;
import net.minestom.server.network.packet.server.play.ClearTitlesPacket;
import net.minestom.server.network.packet.server.play.PlayerListHeaderAndFooterPacket;
import net.minestom.server.utils.PacketSendingUtils;

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
    Collection<Player> getPlayers();

    /**
     * Broadcast a ServerPacket to all players of this audience
     *
     * @param packet the packet to broadcast
     */
    default void sendGroupedPacket(ServerPacket packet) {
        PacketSendingUtils.sendGroupedPacket(getPlayers(), packet);
    }

    @Deprecated
    @Override
    default void sendMessage(Identity source, Component message, MessageType type) {
        Messenger.sendMessage(this.getPlayers(), message, ChatPosition.fromMessageType(type), source.uuid());
    }

    @Override
    default void sendActionBar(Component message) {
        sendGroupedPacket(new ActionBarPacket(message));
    }

    @Override
    default void sendPlayerListHeaderAndFooter(Component header, Component footer) {
        sendGroupedPacket(new PlayerListHeaderAndFooterPacket(header, footer));
    }

    @Override
    default <T> void sendTitlePart(TitlePart<T> part, T value) {
        sendGroupedPacket(AdventurePacketConvertor.createTitlePartPacket(part, value));
    }

    @Override
    default void clearTitle() {
        sendGroupedPacket(new ClearTitlesPacket(false));
    }

    @Override
    default void resetTitle() {
        sendGroupedPacket(new ClearTitlesPacket(true));
    }

    @Override
    default void showBossBar(BossBar bar) {
        MinecraftServer.getBossBarManager().addBossBar(this.getPlayers(), bar);
    }

    @Override
    default void hideBossBar(BossBar bar) {
        MinecraftServer.getBossBarManager().removeBossBar(this.getPlayers(), bar);
    }

    /**
     * Plays a {@link Sound} at a given point
     * @param sound The sound to play
     * @param point The point in this instance at which to play the sound
     */
    default void playSound(Sound sound, Point point) {
        playSound(sound, point.x(), point.y(), point.z());
    }

    @Override
    default void playSound(Sound sound, double x, double y, double z) {
        sendGroupedPacket(AdventurePacketConvertor.createSoundPacket(sound, x, y, z));
    }

    @Override
    default void playSound(Sound sound, Sound.Emitter emitter) {
        if (emitter != Sound.Emitter.self()) {
            sendGroupedPacket(AdventurePacketConvertor.createSoundPacket(sound, emitter));
        } else {
            // if we're playing on self, we need to delegate to each audience member
            for (Audience audience : this.audiences()) {
                audience.playSound(sound, emitter);
            }
        }
    }

    @Override
    default void stopSound(SoundStop stop) {
        sendGroupedPacket(AdventurePacketConvertor.createSoundStopPacket(stop));
    }

    /**
     * Send a {@link Notification} to the audience.
     * @param notification the {@link Notification} to send
     */
    default void sendNotification(Notification notification) {
        sendGroupedPacket(notification.buildAddPacket());
        sendGroupedPacket(notification.buildRemovePacket());
    }

    @Override
    default Iterable<? extends Audience> audiences() {
        return this.getPlayers();
    }
}
