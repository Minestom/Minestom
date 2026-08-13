package net.minestom.server.advancements;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class AdvancementIntegrationTest {

    @Test
    public void addAndRemoveViewer(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 42, 0));

        AdvancementRoot root = new AdvancementRoot(
                Component.text("title"),
                Component.text("description"),
                Material.DIAMOND,
                FrameType.TASK,
                0,
                0,
                "minecraft:textures/block/stone.png"
        );

        AdvancementTab tab = env.process().advancement().createTab("minestom:minestom_tab", root);

        // Add viewer
        tab.addViewer(player);
        assertEquals(1, tab.getViewers().size());
        assertTrue(tab.getViewers().contains(player));

        assertNotNull(AdvancementTab.getTabs(player));
        assertEquals(1, AdvancementTab.getTabs(player).size());
        assertTrue(AdvancementTab.getTabs(player).contains(tab));

        // Remove viewer
        tab.removeViewer(player);
        assertEquals(0, tab.getViewers().size());

        assertNull(AdvancementTab.getTabs(player));
    }

    @Test
    public void removeViewerOnDisconnect(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 42, 0));

        AdvancementRoot root1 = new AdvancementRoot(
                Component.text("title"),
                Component.text("description"),
                Material.DIAMOND,
                FrameType.TASK,
                0,
                0,
                "minecraft:textures/block/stone.png"
        );

        AdvancementRoot root2 = new AdvancementRoot(
                Component.text("title2"),
                Component.text("description"),
                Material.DIAMOND,
                FrameType.TASK,
                0,
                0,
                "minecraft:textures/block/stone.png"
        );

        AdvancementTab tab1 = env.process().advancement().createTab("minestom:minestom_tab1", root1);
        AdvancementTab tab2 = env.process().advancement().createTab("minestom:minestom_tab2", root2);
        tab1.addViewer(player);
        tab2.addViewer(player);

        player.remove(); // Disconnect
        assertEquals(0, tab1.getViewers().size());
        assertEquals(0, tab2.getViewers().size());
        assertNull(AdvancementTab.getTabs(player));
    }

    @Test
    public void unregisterAdvancementTab(Env env) {
        final Instance instance = env.createFlatInstance();
        final Player player1 = env.createPlayer(instance, new Pos(0, 42, 0));
        final Player player2 = env.createPlayer(instance, new Pos(0, 42, 0));

        final AdvancementRoot root1 = new AdvancementRoot(
                Component.text("title"),
                Component.text("description"),
                Material.DIAMOND,
                FrameType.TASK,
                0,
                0,
                "minecraft:textures/block/stone.png"
        );

        final AdvancementRoot root2 = new AdvancementRoot(
                Component.text("title2"),
                Component.text("description"),
                Material.DIAMOND,
                FrameType.TASK,
                0,
                0,
                "minecraft:textures/block/stone.png"
        );

        final AdvancementRoot root3 = new AdvancementRoot(
                Component.text("title3"),
                Component.text("description"),
                Material.DIAMOND,
                FrameType.TASK,
                0,
                0,
                "minecraft:textures/block/stone.png"
        );

        final AdvancementTab tab1 = env.process().advancement().createTab("minestom:minestom_tab1", root1);
        final AdvancementTab tab2 = env.process().advancement().createTab("minestom:minestom_tab2", root2);
        final AdvancementTab tab3 = env.process().advancement().createTab("minestom:minestom_tab3", root3);

        tab1.addViewer(player1);
        tab1.addViewer(player2);

        tab2.addViewer(player1);
        tab2.addViewer(player2);

        tab3.addViewer(player2);

        assertEquals(2, tab1.getViewers().size());
        final AdvancementTab tab1Removed = env.process().advancement().removeTab(tab1.getRoot().getIdentifier(), true);
        assertNotNull(tab1Removed);
        assertEquals(0, tab1Removed.getViewers().size());
        assertEquals(0, tab1Removed.getViewers().size());
        assertNull(env.process().advancement().removeTab(tab1.getRoot().getIdentifier(), true));
        assertNull(env.process().advancement().removeTab(tab1.getRoot().getIdentifier(), false));

        final AdvancementTab tab2Removed = env.process().advancement().removeTab(tab2.getRoot().getIdentifier(), false);
        assertNotNull(tab2Removed);
        assertEquals(2, tab2Removed.getViewers().size());
        assertTrue(tab2Removed.isViewer(player1));
        assertTrue(tab2Removed.isViewer(player2));
        assertNull(env.process().advancement().removeTab(tab2.getRoot().getIdentifier(), true));
        assertNull(env.process().advancement().removeTab(tab2.getRoot().getIdentifier(), false));

        final AdvancementTab tab3Removed = env.process().advancement().removeTab(tab3.getRoot().getIdentifier(), false);
        assertNotNull(tab3Removed);
        assertEquals(1, tab3Removed.getViewers().size());
        assertTrue(tab3Removed.isViewer(player2));
        assertNull(env.process().advancement().removeTab(tab3.getRoot().getIdentifier(), true));
        assertNull(env.process().advancement().removeTab(tab3.getRoot().getIdentifier(), false));
    }
}
