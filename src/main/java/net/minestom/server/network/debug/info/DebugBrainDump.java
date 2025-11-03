package net.minestom.server.network.debug.info;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import java.util.List;
import java.util.Set;

public record DebugBrainDump(
        String name,
        String profession,
        int xp,
        float health,
        float maxHealth,
        String inventory,
        boolean wantsGolen,
        int angerLevel,
        List<String> activities,
        List<String> behaviors,
        List<String> memories,
        List<String> gossips,
        Set<Point> pois,
        Set<Point> potentialPois
) {
    public static NetworkBuffer.Type<DebugBrainDump> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.STRING, DebugBrainDump::name,
            NetworkBuffer.STRING, DebugBrainDump::profession,
            NetworkBuffer.INT, DebugBrainDump::xp,
            NetworkBuffer.FLOAT, DebugBrainDump::health,
            NetworkBuffer.FLOAT, DebugBrainDump::maxHealth,
            NetworkBuffer.STRING, DebugBrainDump::inventory,
            NetworkBuffer.BOOLEAN, DebugBrainDump::wantsGolen,
            NetworkBuffer.INT, DebugBrainDump::angerLevel,
            NetworkBuffer.STRING.list(), DebugBrainDump::activities,
            NetworkBuffer.STRING.list(), DebugBrainDump::behaviors,
            NetworkBuffer.STRING.list(), DebugBrainDump::memories,
            NetworkBuffer.STRING.list(), DebugBrainDump::gossips,
            NetworkBuffer.BLOCK_POSITION.set(), DebugBrainDump::pois,
            NetworkBuffer.BLOCK_POSITION.set(), DebugBrainDump::potentialPois,
            DebugBrainDump::new);

    public DebugBrainDump {
        activities = List.copyOf(activities);
        behaviors = List.copyOf(behaviors);
        memories = List.copyOf(memories);
        gossips = List.copyOf(gossips);
        pois = Set.copyOf(pois);
        potentialPois = Set.copyOf(potentialPois);
    }
}
