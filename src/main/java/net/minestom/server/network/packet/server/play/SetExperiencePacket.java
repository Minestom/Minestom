package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.FLOAT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record SetExperiencePacket(float percentage, int level, int totalExperience) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SetExperiencePacket> SERIALIZER = NetworkBufferTemplate.template(
            FLOAT, SetExperiencePacket::percentage,
            VAR_INT, SetExperiencePacket::level,
            VAR_INT, SetExperiencePacket::totalExperience,
            SetExperiencePacket::new);
}
