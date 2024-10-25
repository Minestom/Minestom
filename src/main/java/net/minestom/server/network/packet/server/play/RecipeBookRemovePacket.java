package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record RecipeBookRemovePacket(@NotNull List<Integer> displayIds) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<RecipeBookRemovePacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT.list(), RecipeBookRemovePacket::displayIds,
            RecipeBookRemovePacket::new);

    public RecipeBookRemovePacket {
        displayIds = List.copyOf(displayIds);
    }

}
