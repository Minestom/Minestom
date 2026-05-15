package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;
import net.minestom.demo.feature.entities.EntitiesFeature;
import net.minestom.demo.feature.waypoint.WaypointFeature;

/**
 * Pairs {@link EntitiesFeature} (which sends tracked waypoints anchored
 * to each spawned NPC) with {@link WaypointFeature} (which sends a
 * coordinate-only waypoint at first spawn), so every flavor of the
 * waypoint API is visible at once.
 */
public final class WaypointServer {

    static void main(String[] args) {
        DemoServer.create().features(
                new LobbyFeature(),
                new EntitiesFeature(),
                new WaypointFeature()
        ).start();
    }
}
