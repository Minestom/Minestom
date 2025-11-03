package net.minestom.server.network.debug;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.debug.info.*;
import net.minestom.server.utils.Unit;

import java.util.List;

@SuppressWarnings("unused")
sealed interface DebugSubscriptions permits DebugSubscription {
    DebugSubscription<Unit> DEDICATED_SERVER_TICK_TIME = register("dedicated_server_tick_time");
    DebugSubscription<DebugBeeInfo> BEES = register("bees", DebugBeeInfo.SERIALIZER);
    DebugSubscription<DebugBrainDump> BRAINS = register("brains", DebugBrainDump.SERIALIZER);
    DebugSubscription<DebugBreezeInfo> BREEZES = register("breezes", DebugBreezeInfo.SERIALIZER);
    DebugSubscription<List<DebugGoalInfo>> GOAL_SELECTORS = register("goal_selectors", DebugGoalInfo.SERIALIZER.list());
    DebugSubscription<DebugPathInfo> ENTITY_PATHS = register("entity_paths", DebugPathInfo.SERIALIZER);
    DebugSubscription<DebugEntityBlockIntersection> ENTITY_BLOCK_INTERSECTIONS = register("entity_block_intersections", DebugEntityBlockIntersection.SERIALIZER);
    DebugSubscription<DebugHiveInfo> BEE_HIVES = register("bee_hives", DebugHiveInfo.SERIALIZER);
    DebugSubscription<DebugPoiInfo> POIS = register("pois", DebugPoiInfo.SERIALIZER);
    DebugSubscription<Integer> REDSTONE_WIRE_ORIENTATIONS = register("redstone_wire_orientations", NetworkBuffer.VAR_INT);
    DebugSubscription<Unit> VILLAGE_SECTIONS = register("village_sections");
    DebugSubscription<List<Point>> RAIDS = register("raids", NetworkBuffer.BLOCK_POSITION.list());
    DebugSubscription<List<DebugStructureInfo>> STRUCTURES = register("structures", DebugStructureInfo.SERIALIZER.list());
    DebugSubscription<DebugGameEventListenerInfo> GAME_EVENT_LISTENERS = register("game_event_listeners", DebugGameEventListenerInfo.SERIALIZER);
    DebugSubscription<Point> NEIGHBOR_UPDATES = register("neighbor_updates", NetworkBuffer.BLOCK_POSITION);
    DebugSubscription<DebugGameEventInfo> GAME_EVENTS = register("game_events", DebugGameEventInfo.SERIALIZER);

    private static DebugSubscription<Unit> register(String name) {
        return register(name, NetworkBuffer.UNIT);
    }

    private static <T> DebugSubscription<T> register(String name, NetworkBuffer.Type<T> networkType) {
        DebugSubscription<T> impl = new DebugSubscriptionImpl<>(DebugSubscriptionImpl.NAMESPACES.size(), Key.key(name), networkType);
        DebugSubscriptionImpl.NAMESPACES.put(impl.name(), impl);
        DebugSubscriptionImpl.IDS.set(impl.id(), impl);
        return impl;
    }
}
