package net.minestom.server.entity.player;

import net.kyori.adventure.resource.ResourcePackCallback;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.client.common.ClientResourcePackStatusPacket;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.kyori.adventure.resource.ResourcePackRequest.resourcePackRequest;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MicrotusExtension.class)
class PlayerResourcePackIntegrationTest {
    private static final ResourcePackInfo INFO = ResourcePackInfo.resourcePackInfo(UUID.randomUUID(), URI.create("http://localhost:8080/missing.zip"), "i am not a hash!");

    @Test
    void applyCallbackOnSuccess(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 40, 0));

        AtomicBoolean called = new AtomicBoolean();
        ResourcePackCallback callback = (uuid, resourcePackStatus, audience) -> called.set(true);

        player.sendResourcePacks(resourcePackRequest().callback(callback).packs(INFO).build());
        player.addPacketToQueue(new ClientResourcePackStatusPacket(INFO.id(), ResourcePackStatus.SUCCESSFULLY_LOADED));

        player.interpretPacketQueue();

        assertTrue(called.get());
        assertTrue(player.isOnline());
    }

    @Test
    void applyFailRequiredKicksPlayer(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 40, 0));

        player.sendResourcePacks(resourcePackRequest().required(true).packs(INFO).build());
        player.addPacketToQueue(new ClientResourcePackStatusPacket(INFO.id(), ResourcePackStatus.FAILED_RELOAD));

        player.interpretPacketQueue();

        assertFalse(player.isOnline());
    }

    @Test
    void applyFailNotRequiredDoesNotKickPlayer(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 40, 0));

        player.sendResourcePacks(resourcePackRequest().required(false).packs(INFO).build());
        player.addPacketToQueue(new ClientResourcePackStatusPacket(INFO.id(), ResourcePackStatus.FAILED_RELOAD));

        player.interpretPacketQueue();

        assertTrue(player.isOnline());
    }
}
