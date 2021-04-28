package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.function.UnaryOperator;

/**
 * Packet sent during combat to a {@link Player}.
 * Only death is supported for the moment (other events are ignored anyway as of 1.15.2)
 */
public class CombatEventPacket implements ComponentHoldingServerPacket {

    private EventType type = EventType.ENTER_COMBAT;
    private int duration;
    private int opponent;
    private int playerID;
    private Component deathMessage;

    private CombatEventPacket() {
    }

    public static CombatEventPacket enter() {
        CombatEventPacket packet = new CombatEventPacket();
        packet.type = EventType.ENTER_COMBAT;
        return packet;
    }

    public static CombatEventPacket end(int durationInTicks, Entity opponent) {
        CombatEventPacket packet = new CombatEventPacket();
        packet.type = EventType.END_COMBAT;
        packet.duration = durationInTicks;
        packet.opponent = opponent != null ? opponent.getEntityId() : -1;
        return packet;
    }

    public static CombatEventPacket death(Player player, Entity killer, Component message) {
        CombatEventPacket packet = new CombatEventPacket();
        packet.type = EventType.DEATH;
        packet.playerID = player.getEntityId();
        packet.opponent = killer != null ? killer.getEntityId() : -1;
        packet.deathMessage = message;
        return packet;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
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
                writer.writeComponent(deathMessage);
                break;
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        type = EventType.values()[reader.readVarInt()];
        switch (type) {
            case ENTER_COMBAT:
                // nothing to add
                break;

            case END_COMBAT:
                duration = reader.readVarInt();
                opponent = reader.readInt();
                break;

            case DEATH:
                playerID = reader.readVarInt();
                opponent = reader.readInt();
                deathMessage = reader.readComponent(Integer.MAX_VALUE);
                break;
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.COMBAT_EVENT;
    }

    @Override
    public @NotNull Collection<Component> components() {
        if (this.type == EventType.DEATH) {
            return Collections.singleton(deathMessage);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        if (this.type == EventType.DEATH) {
            CombatEventPacket packet = new CombatEventPacket();
            packet.type = type;
            packet.playerID = playerID;
            packet.opponent = opponent;
            packet.deathMessage = deathMessage;
            return packet;
        } else {
            return this;
        }
    }

    public enum EventType {
        ENTER_COMBAT, END_COMBAT, // both ignored by Notchian client
        DEATH,
    }
}
