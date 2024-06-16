package net.minestom.server.advancements;

import net.kyori.adventure.text.Component;
import net.minestom.testing.Env;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.Material;
import net.minestom.testing.extension.MicrotusExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class AdvancementIntegrationTest {

    @Test
    void addAndRemoveViewer(Env env) {
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
    void removeViewerOnDisconnect(Env env) {
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
}
