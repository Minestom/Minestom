package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientGenerateStructurePacket(@NotNull Point blockPosition,
                                            int level, boolean keepJigsaws) implements ClientPacket {
    public ClientGenerateStructurePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BLOCK_POSITION), reader.read(VAR_INT), reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BLOCK_POSITION, blockPosition);
        writer.write(VAR_INT, level);
        writer.write(BOOLEAN, keepJigsaws);
    }
}
