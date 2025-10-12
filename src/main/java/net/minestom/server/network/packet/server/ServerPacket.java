package net.minestom.server.network.packet.server;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.function.UnaryOperator;

/**
 * Represents a packet which can be sent to a player using {@link PlayerConnection#sendPacket(SendablePacket)}.
 * <p>
 * Packets are value-based, and should therefore not be reliant on identity.
 */
public sealed interface ServerPacket extends SendablePacket {

    non-sealed interface Configuration extends ServerPacket {
    }

    non-sealed interface Status extends ServerPacket {
    }

    non-sealed interface Login extends ServerPacket {
    }

    non-sealed interface Play extends ServerPacket {
    }

    interface ComponentHolding extends ComponentHolder<ServerPacket> {
        @Override
        @Unmodifiable
        @Contract(pure = true)
        Collection<Component> components();

        @Override
        @Contract(pure = true)
        ServerPacket copyWithOperator(UnaryOperator<Component> operator);
    }
}
