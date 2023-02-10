package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.GameMode;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record JoinGamePacket(int entityId, boolean isHardcore, GameMode gameMode, GameMode previousGameMode,
                             List<String> worlds, NBTCompound dimensionCodec, String dimensionType, String world,
                             long hashedSeed, int maxPlayers, int viewDistance, int simulationDistance,
                             boolean reducedDebugInfo, boolean enableRespawnScreen, boolean isDebug,
                             boolean isFlat) implements ServerPacket {
    public JoinGamePacket {
        worlds = List.copyOf(worlds);
    }

    public JoinGamePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(BOOLEAN), GameMode.fromId(reader.read(BYTE)), GameMode.fromId(reader.read(BYTE)),
                reader.readCollection(STRING), (NBTCompound) reader.read(NBT), reader.read(STRING), reader.read(STRING),
                reader.read(LONG), reader.read(VAR_INT), reader.read(VAR_INT), reader.read(VAR_INT),
                reader.read(BOOLEAN), reader.read(BOOLEAN), reader.read(BOOLEAN), reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(INT, entityId);
        writer.write(BOOLEAN, isHardcore);
        writer.write(BYTE, gameMode.id());
        if (previousGameMode != null) {
            writer.write(BYTE, previousGameMode.id());
        } else {
            writer.write(BYTE, (byte) -1);
        }

        writer.writeCollection(STRING, worlds);
        writer.write(NBT, dimensionCodec);

        writer.write(STRING, dimensionType);
        writer.write(STRING, world);
        writer.write(LONG, hashedSeed);
        writer.write(VAR_INT, maxPlayers);
        writer.write(VAR_INT, viewDistance);
        writer.write(VAR_INT, simulationDistance);
        writer.write(BOOLEAN, reducedDebugInfo);
        writer.write(BOOLEAN, enableRespawnScreen);
        //debug
        writer.write(BOOLEAN, isDebug);
        //is flat
        writer.write(BOOLEAN, isFlat);

        writer.write(BOOLEAN, false);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.JOIN_GAME;
    }

}
