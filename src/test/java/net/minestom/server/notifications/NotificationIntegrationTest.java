package net.minestom.server.notifications;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.AdvancementsPacket;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
class NotificationIntegrationTest {

    @Test
    void testBuilder(Env env) {
        var notification = Notification.builder()
                .icon(Material.ITEM_FRAME)
                .title(Component.text("unit test"))
                .frameType(FrameType.TASK)
                .build();
        assertEquals(FrameType.TASK, notification.type());
        assertEquals(ItemStack.of(Material.ITEM_FRAME), notification.icon());
        assertEquals(Component.text("unit test"), notification.title());
    }

    @Test
    void testSend(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        Collector<AdvancementsPacket> advancementsPacketCollector = connection.trackIncoming(AdvancementsPacket.class);
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
        var notification = Notification.builder()
                .icon(Material.ITEM_FRAME)
                .title(Component.text("unit test"))
                .frameType(FrameType.TASK)
                .build();
        notification.send(player);
        advancementsPacketCollector.assertCount(2);
        AdvancementsPacket advancementsPacket = advancementsPacketCollector.collect().get(1);
        assertNotNull(advancementsPacket);
        Optional<AdvancementsPacket.AdvancementMapping> advancementMapping = advancementsPacket.advancementMappings().stream().findFirst();
        advancementMapping.ifPresent(advancementMapping1 -> {
            AdvancementsPacket.Advancement advancement = advancementMapping1.value();
            assertFalse(advancement.sendTelemetryData());
            var displayData = advancement.displayData();
            assertEquals(ItemStack.of(Material.ITEM_FRAME), displayData.icon());
            assertEquals(Component.text("unit test"), displayData.title());
            assertEquals(FrameType.TASK, displayData.frameType());
        });
    }
}
