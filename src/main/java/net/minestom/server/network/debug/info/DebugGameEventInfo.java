package net.minestom.server.network.debug.info;

import net.minestom.server.coordinate.Point;
import net.minestom.server.game.GameEvent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

public value record DebugGameEventInfo(GameEvent event, Point position) {
    public static final NetworkBuffer.Type<DebugGameEventInfo> SERIALIZER = NetworkBufferTemplate.template(
            GameEvent.NETWORK_TYPE, DebugGameEventInfo::event,
            NetworkBuffer.VECTOR3, DebugGameEventInfo::position,
            DebugGameEventInfo::new);
}
