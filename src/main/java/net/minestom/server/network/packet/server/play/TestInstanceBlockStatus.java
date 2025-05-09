package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public record TestInstanceBlockStatus(
        @NotNull Component status,
        @Nullable Point size
) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<TestInstanceBlockStatus> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.COMPONENT, TestInstanceBlockStatus::status,
            NetworkBuffer.VECTOR3I, TestInstanceBlockStatus::size,
            TestInstanceBlockStatus::new);

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(status);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new TestInstanceBlockStatus(operator.apply(status), size);
    }
}
