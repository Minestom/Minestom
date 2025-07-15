package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public record TestInstanceBlockStatus(
        Component status,
        @Nullable Point size
) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<TestInstanceBlockStatus> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.COMPONENT, TestInstanceBlockStatus::status,
            NetworkBuffer.VECTOR3I, TestInstanceBlockStatus::size,
            TestInstanceBlockStatus::new);

    @Override
    public Collection<Component> components() {
        return List.of(status);
    }

    @Override
    public ServerPacket copyWithOperator(UnaryOperator<Component> operator) {
        return new TestInstanceBlockStatus(operator.apply(status), size);
    }
}
