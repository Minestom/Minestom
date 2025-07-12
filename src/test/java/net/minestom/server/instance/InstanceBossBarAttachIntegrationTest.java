package net.minestom.server.instance;

import net.kyori.adventure.bossbar.BossBar;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.BossBarPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static net.kyori.adventure.text.Component.text;
import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class InstanceBossBarAttachIntegrationTest {

    private static BossBar sampleBossBar() {
        return BossBar.bossBar(text("Test Boss Bar"), 1.0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
    }

    @Test
    public void attachReturn(Env env) {
        Instance instance = env.process().instance().createInstanceContainer();
        BossBar bossBar = sampleBossBar();

        assertEquals(0, instance.attachedBossBars().size());
        assertTrue(instance.attachBossBar(bossBar));
        assertFalse(instance.attachBossBar(bossBar));
        assertEquals(1, instance.attachedBossBars().size());
        assertTrue(instance.detachBossBar(bossBar));
        assertFalse(instance.detachBossBar(bossBar));
        assertEquals(0, instance.attachedBossBars().size());
    }

    @Test
    public void showOnAttach(Env env) {
        Instance instance = env.process().instance().createInstanceContainer();
        BossBar bossBar = sampleBossBar();

        var connection = env.createConnection();
        connection.connect(instance, new Pos(0, 40, 0));

        var collector = connection.trackIncoming(BossBarPacket.class);
        instance.attachBossBar(bossBar);
        collector.assertSingle(bossBarPacket -> assertInstanceOf(BossBarPacket.AddAction.class, bossBarPacket.action()));
    }

    @Test
    public void hideOnDetach(Env env) {
        Instance instance = env.process().instance().createInstanceContainer();
        BossBar bossBar = sampleBossBar();

        var connection = env.createConnection();
        connection.connect(instance, new Pos(0, 40, 0));
        instance.attachBossBar(bossBar);
        var collector = connection.trackIncoming(BossBarPacket.class);
        instance.detachBossBar(bossBar);
        collector.assertSingle(bossBarPacket -> assertInstanceOf(BossBarPacket.RemoveAction.class, bossBarPacket.action()));
    }

    @Test
    public void showOnAdd(Env env) {
        Instance instance = env.process().instance().createInstanceContainer();
        BossBar bossBar = sampleBossBar();
        instance.attachBossBar(bossBar);

        var connection = env.createConnection();
        var collector = connection.trackIncoming(BossBarPacket.class);
        connection.connect(instance, new Pos(0, 40, 0));
        collector.assertSingle(bossBarPacket -> assertInstanceOf(BossBarPacket.AddAction.class, bossBarPacket.action()));
    }

    @Test
    public void hideOnRemove(Env env) {
        Instance instance = env.process().instance().createInstanceContainer();
        Instance instance2 = env.process().instance().createInstanceContainer();
        BossBar bossBar = sampleBossBar();

        var connection = env.createConnection();
        Player player = connection.connect(instance, new Pos(0, 40, 0));
        instance.attachBossBar(bossBar);
        var collector = connection.trackIncoming(BossBarPacket.class);
        player.setInstance(instance2).join();
        collector.assertSingle(bossBarPacket -> assertInstanceOf(BossBarPacket.RemoveAction.class, bossBarPacket.action()));
    }

    @Test
    public void update(Env env) {
        Instance instance = env.process().instance().createInstanceContainer();
        BossBar bossBar = sampleBossBar();
        instance.attachBossBar(bossBar);

        var connection = env.createConnection();
        connection.connect(instance, new Pos(0, 40, 0));
        var collector = connection.trackIncoming(BossBarPacket.class);
        bossBar.name(text("Text update"));
        collector.assertSingle(bossBarPacket -> assertInstanceOf(BossBarPacket.UpdateTitleAction.class, bossBarPacket.action()));
    }
}
