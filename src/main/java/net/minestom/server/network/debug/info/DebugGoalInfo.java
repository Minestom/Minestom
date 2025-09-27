package net.minestom.server.network.debug.info;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

public record DebugGoalInfo(int priority, boolean isRunning, String name) {
    public static final NetworkBuffer.Type<DebugGoalInfo> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, DebugGoalInfo::priority,
            NetworkBuffer.BOOLEAN, DebugGoalInfo::isRunning,
            NetworkBuffer.STRING, DebugGoalInfo::name,
            DebugGoalInfo::new);
}
