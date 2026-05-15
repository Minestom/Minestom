package net.minestom.demo.feature.waypoint;

import net.minestom.demo.core.Feature;
import net.minestom.server.ServerProcess;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.network.packet.server.play.TrackedWaypointPacket;
import net.minestom.server.utils.Either;

import java.util.UUID;

/** Coordinate-only {@link TrackedWaypointPacket} on first spawn. */
public final class WaypointFeature implements Feature {

    private static final UUID DEMO_WAYPOINT = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    public void register(ServerProcess process) {
        process.eventHandler().addListener(PlayerSpawnEvent.class, event -> {
            if (!event.isFirstSpawn()) return;
            event.getPlayer().sendPacket(new TrackedWaypointPacket(
                    TrackedWaypointPacket.Operation.TRACK,
                    new TrackedWaypointPacket.Waypoint(
                            Either.left(DEMO_WAYPOINT),
                            TrackedWaypointPacket.Icon.DEFAULT,
                            new TrackedWaypointPacket.Target.Vec3i(new Pos(20, 60, 20)))));
        });
    }
}
