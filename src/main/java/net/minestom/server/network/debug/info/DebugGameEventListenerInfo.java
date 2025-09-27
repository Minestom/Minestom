package net.minestom.server.network.debug.info;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

public record DebugGameEventListenerInfo(int listenerRadius) {
    public static final NetworkBuffer.Type<DebugGameEventListenerInfo> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, DebugGameEventListenerInfo::listenerRadius,
            DebugGameEventListenerInfo::new);
}
