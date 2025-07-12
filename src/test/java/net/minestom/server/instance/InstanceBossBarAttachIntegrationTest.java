package net.minestom.server.instance;

import net.kyori.adventure.bossbar.BossBar;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.BossBarPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static net.kyori.adventure.text.Component.text;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class InstanceBossBarAttachIntegrationTest {

    private static BossBar sampleBossBar() {
        return BossBar.bossBar(text("Test Boss Bar"), 1.0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
    }

    @Test
    public void attachReturn(Env env) {
        Instance instance = env.process().instance().createInstanceContainer();
        BossBar bossBar = sampleBossBar();

        assertTrue(instance.attachBossBar(bossBar));
        assertFalse(instance.attachBossBar(bossBar));
        assertTrue(instance.detachBossBar(bossBar));
        assertFalse(instance.detachBossBar(bossBar));
    }

    @Test
    public void showOnAttach(Env env) {
        Instance instance = env.process().instance().createInstanceContainer();
        BossBar bossBar = sampleBossBar();

        var connection = env.createConnection();
        connection.connect(instance, new Pos(0, 40, 0));

        var collector = connection.trackIncoming(BossBarPacket.class);
        instance.attachBossBar(bossBar);
        collector.assertSingle();
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
        collector.assertSingle();
    }

    @Test
    public void showOnAdd(Env env) {
        Instance instance = env.process().instance().createInstanceContainer();
        BossBar bossBar = sampleBossBar();
        instance.attachBossBar(bossBar);

        var connection = env.createConnection();
        var collector = connection.trackIncoming(BossBarPacket.class);
        connection.connect(instance, new Pos(0, 40, 0));
        collector.assertSingle();
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
        collector.assertSingle();
    }
}
