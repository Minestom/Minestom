package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;
import net.minestom.demo.feature.entities.EntitiesFeature;
import net.minestom.demo.feature.waypoint.WaypointFeature;

/**
 * Entity zoo: NPCs (happy ghast, copper golem, custom-skin player,
 * mannequin), the entity commands, plus a coordinate-only tracked
 * waypoint via {@link WaypointFeature}.
 */
public final class EntitiesServer {

    static void main(String[] args) {
        DemoServer.create().features(
                new LobbyFeature(),
                new EntitiesFeature(),
                new WaypointFeature()
        ).start();
    }
}
