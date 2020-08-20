package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.ColoredText;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.Optional;

/**
 * Packet sent during combat to a player.
 * Only death is supported for the moment (other events are ignored anyway as of 1.15.2)
 */
public class CombatEventPacket implements ServerPacket {

    private EventType type;
    private int duration;
    private int opponent;
    private int playerID;
    private ColoredText deathMessage;

    private CombatEventPacket() {
    }

    public static CombatEventPacket enter() {
        CombatEventPacket packet = new CombatEventPacket();
        packet.type = EventType.ENTER_COMBAT;
        return packet;
    }

    public static CombatEventPacket end(int durationInTicks, Optional<Entity> opponent) {
        CombatEventPacket packet = new CombatEventPacket();
        packet.type = EventType.END_COMBAT;
        packet.duration = durationInTicks;
        packet.opponent = opponent.map(Entity::getEntityId).orElse(-1);
        return packet;
    }

    public static CombatEventPacket death(Player player, Optional<Entity> killer, ColoredText message) {
        CombatEventPacket packet = new CombatEventPacket();
        packet.type = EventType.DEATH;
        packet.playerID = player.getEntityId();
        packet.opponent = killer.map(Entity::getEntityId).orElse(-1);
        packet.deathMessage = message;
        return packet;
    }

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(type.ordinal());
        switch (type) {
            case ENTER_COMBAT:
                // nothing to add
                break;

            case END_COMBAT:
                writer.writeVarInt(duration);
                writer.writeInt(opponent);
                break;

            case DEATH:
                writer.writeVarInt(playerID);
                writer.writeInt(opponent);
                writer.writeSizedString(deathMessage.toString());
                break;
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.COMBAT_EVENT;
    }

    public enum EventType {
        ENTER_COMBAT, END_COMBAT, // both ignored by Notchian client
        DEATH,
    }
}
