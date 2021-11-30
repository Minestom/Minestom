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
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.entity.Player;
import net.minestom.server.message.ChatPosition;
import net.minestom.server.message.Messenger;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.ActionBarPacket;
import net.minestom.server.network.packet.server.play.ClearTitlesPacket;
import net.minestom.server.network.packet.server.play.PlayerListHeaderAndFooterPacket;
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

    /**
     * Broadcast a ServerPacket to all players of this audience
     *
     * @param packet the packet to broadcast
     */
    default void sendGroupedPacket(ServerPacket packet) {
        PacketUtils.sendGroupedPacket(this.getPlayers(), packet);
    }

    @Override
    default void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        Messenger.sendMessage(this.getPlayers(), message, ChatPosition.fromMessageType(type), source.uuid());
    }

    @Override
    default void sendActionBar(@NotNull Component message) {
        sendGroupedPacket(new ActionBarPacket(message));
    }

    @Override
    default void sendPlayerListHeaderAndFooter(@NotNull Component header, @NotNull Component footer) {
        sendGroupedPacket(new PlayerListHeaderAndFooterPacket(header, footer));
    }

    @Override
    default <T> void sendTitlePart(@NotNull TitlePart<T> part, @NotNull T value) {
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
    default void showBossBar(@NotNull BossBar bar) {
        MinecraftServer.getBossBarManager().addBossBar(this.getPlayers(), bar);
    }

    @Override
    default void hideBossBar(@NotNull BossBar bar) {
        MinecraftServer.getBossBarManager().removeBossBar(this.getPlayers(), bar);
    }

    @Override
    default void playSound(@NotNull Sound sound, double x, double y, double z) {
        sendGroupedPacket(AdventurePacketConvertor.createSoundPacket(sound, x, y, z));
    }

    @Override
    default void playSound(@NotNull Sound sound, Sound.@NotNull Emitter emitter) {
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
    default void stopSound(@NotNull SoundStop stop) {
        sendGroupedPacket(AdventurePacketConvertor.createSoundStopPacket(stop));
    }

    @Override
    default @NotNull Iterable<? extends Audience> audiences() {
        return this.getPlayers();
    }
}
