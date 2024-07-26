package net.minestom.server.network;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.ServerPacket;

public interface AntiCheat {
    void consume(ServerPacket.Play serverPacket);

    Action consume(ClientPacket clientPacket, ConnectionState connectionState);

    sealed interface Action {
        /**
         * Shall be interpreted.
         */
        record Valid() implements Action {
        }

        /**
         * Must not be interpreted but does not necessarily require action.
         */
        record InvalidIgnore(String message) implements Action {
        }

        /**
         * Must not be interpreted and requires action.
         */
        record InvalidCritical(String message) implements Action {
        }
    }
}
