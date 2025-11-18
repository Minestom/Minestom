package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

public record UpdateScorePacket(
        String entityName,
        String objectiveName,
        int score,
        @Nullable Component displayName,
        @Nullable Sidebar.NumberFormat numberFormat
) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<UpdateScorePacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, UpdateScorePacket::entityName,
            STRING, UpdateScorePacket::objectiveName,
            VAR_INT, UpdateScorePacket::score,
            COMPONENT.optional(), UpdateScorePacket::displayName,
            Sidebar.NumberFormat.SERIALIZER.optional(), UpdateScorePacket::numberFormat,
            UpdateScorePacket::new
    );

    @Override
    public Collection<Component> components() {
        List<Component> list = new ArrayList<>();

        if (displayName != null) {
            list.add(displayName);
        }

        if (numberFormat != null) {
            list.addAll(numberFormat.components());
        }

        return List.copyOf(list);
    }

    @Override
    public ServerPacket copyWithOperator(UnaryOperator<Component> operator) {
        if (displayName == null && numberFormat == null) return this;

        Component name = displayName;
        if (displayName != null) {
            name = operator.apply(displayName);
        }

        Sidebar.NumberFormat format = numberFormat;
        if (numberFormat != null) {
            format = numberFormat.copyWithOperator(operator);
        }


        return new UpdateScorePacket(
                entityName,
                objectiveName,
                score,
                name,
                format
        );
    }
}
