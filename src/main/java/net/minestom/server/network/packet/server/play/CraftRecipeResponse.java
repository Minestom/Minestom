package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record CraftRecipeResponse(byte windowId, String recipe) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<CraftRecipeResponse> SERIALIZER = NetworkBufferTemplate.template(
            BYTE, CraftRecipeResponse::windowId,
            STRING, CraftRecipeResponse::recipe,
            CraftRecipeResponse::new);
}
