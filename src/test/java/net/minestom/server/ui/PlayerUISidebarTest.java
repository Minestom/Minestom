package net.minestom.server.ui;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerUISidebarTest {

    private static final int ADD_OBJECTIVE = 0, UPDATE_SCORE = 0;
    private static final int REMOVE_SCORE = 1;
    private static final int CHANGE_DISPLAY_OBJECTIVE = 2;

    private static final Component COMPONENT_TITLE = Component.text("5D86CB33");
    private static final Component COMPONENT_LINE0 = Component.text("CEF3408E");
    private static final Component COMPONENT_LINE1 = Component.text("33140F0A");
    private static final Component COMPONENT_LINE2 = Component.text("44F23667");
    private static final Component COMPONENT_DIFF = Component.text("822BD528");

    @Test
    public void sidebarInitial() {
        var ui = PlayerUI.newPlayerUI();
        assertTrue(ui.sidebar(sidebar().build()));

        AtomicReference<String> objectiveName = new AtomicReference<>();
        AtomicReference<String> entityName = new AtomicReference<>();

        assertPackets(ui,
                packetCond(ScoreboardObjectivePacket.class, p -> {
                    assertEquals(ADD_OBJECTIVE, p.mode());
                    assertEquals(COMPONENT_TITLE, p.objectiveValue());
                    objectiveName.set(p.objectiveName());
                }),
                packetCond(DisplayScoreboardPacket.class, p -> {
                    assertEquals((byte) 1, p.position());
                    assertEquals(objectiveName.get(), p.scoreName());
                }),
                packetCond(TeamsPacket.class, p -> {
                    assertEquals(p.action().getClass(), TeamsPacket.CreateTeamAction.class);
                    if (p.action() instanceof TeamsPacket.CreateTeamAction createTeamAction) {
                        assertEquals(COMPONENT_LINE0, createTeamAction.teamPrefix());
                        assertEquals(1, createTeamAction.entities().size());
                        entityName.set(createTeamAction.entities().iterator().next());
                    }
                }),
                packetCond(UpdateScorePacket.class, p -> {
                    assertEquals(objectiveName.get(), p.objectiveName());
                    assertEquals(entityName.get(), p.entityName());
                    assertEquals(UPDATE_SCORE, p.action());
                }),
                packetCond(TeamsPacket.class, p -> {
                    assertEquals(p.action().getClass(), TeamsPacket.CreateTeamAction.class);
                    if (p.action() instanceof TeamsPacket.CreateTeamAction createTeamAction) {
                        assertEquals(COMPONENT_LINE1, createTeamAction.teamPrefix());
                        assertEquals(1, createTeamAction.entities().size());
                        entityName.set(createTeamAction.entities().iterator().next());
                    }
                }),
                packetCond(UpdateScorePacket.class, p -> {
                    assertEquals(objectiveName.get(), p.objectiveName());
                    assertEquals(entityName.get(), p.entityName());
                    assertEquals(UPDATE_SCORE, p.action());
                })
        );
    }

    @Test
    public void sidebarIdenticalNoPackets() {
        var ui = PlayerUI.newPlayerUI();
        assertTrue(ui.sidebar(sidebar().build()));
        ui.drain(packet -> {});

        // Identical sidebar shouldn't send any packets
        assertFalse(ui.sidebar(sidebar().build()));
        ui.drain(packet -> { throw new IllegalStateException(); });
    }

    @Test
    public void sidebarUpdateLine() {
        var ui = PlayerUI.newPlayerUI();
        assertTrue(ui.sidebar(sidebar().build()));
        ui.drain(packet -> {});

        // Sending a new line
        assertTrue(ui.sidebar(sidebar()
                .add(COMPONENT_LINE2)
                .build()));

        AtomicReference<String> entityName = new AtomicReference<>();

        assertPackets(ui,
                packetCond(TeamsPacket.class, p -> {
                    assertEquals(p.action().getClass(), TeamsPacket.CreateTeamAction.class);
                    if (p.action() instanceof TeamsPacket.CreateTeamAction createTeamAction) {
                        assertEquals(COMPONENT_LINE2, createTeamAction.teamPrefix());
                        assertEquals(1, createTeamAction.entities().size());
                        entityName.set(createTeamAction.entities().iterator().next());
                    }
                }),
                packetCond(UpdateScorePacket.class, p -> {
                    assertEquals(entityName.get(), p.entityName());
                    assertEquals(UPDATE_SCORE, p.action());
                })
        );

        // Updating an existing line
        assertTrue(ui.sidebar(sidebar()
                .add(COMPONENT_DIFF)
                .build()));

        assertPackets(ui,
                packetCond(TeamsPacket.class, p -> {
                    assertEquals(p.action().getClass(), TeamsPacket.UpdateTeamAction.class);
                    if (p.action() instanceof TeamsPacket.UpdateTeamAction updateTeamAction) {
                        assertEquals(COMPONENT_DIFF, updateTeamAction.teamPrefix());
                    }
                })
        );

        // Ensure state is actually changed
        assertFalse(ui.sidebar(sidebar()
                .add(COMPONENT_DIFF)
                .build()));
        ui.drain(packet -> { throw new IllegalStateException(); });
    }

    @Test
    public void sidebarUpdateTitle() {
        var ui = PlayerUI.newPlayerUI();
        assertTrue(ui.sidebar(sidebar().build()));
        ui.drain(packet -> {});

        // Updating the title
        assertTrue(ui.sidebar(sidebar()
                .title(COMPONENT_DIFF)
                .build()));

        assertPackets(ui,
                packetCond(ScoreboardObjectivePacket.class, p -> {
                    assertEquals(CHANGE_DISPLAY_OBJECTIVE, p.mode());
                    assertEquals(COMPONENT_DIFF, p.objectiveValue());
                })
        );

        // Ensure state is actually changed
        assertFalse(ui.sidebar(sidebar()
                .title(COMPONENT_DIFF)
                .build()));
        ui.drain(packet -> { throw new IllegalStateException(); });
    }

    @Test
    public void sidebarRemoveLine() {
        var ui = PlayerUI.newPlayerUI();
        ui.sidebar(sidebar().build());
        ui.drain(packet -> {});

        // Remove lines 0 and 1
        assertTrue(ui.sidebar(SidebarUI.builder(COMPONENT_TITLE).build()));

        assertPackets(ui,
                packetCond(UpdateScorePacket.class, p -> {
                    assertEquals(REMOVE_SCORE, p.action());
                }),
                packetCond(UpdateScorePacket.class, p -> {
                    assertEquals(REMOVE_SCORE, p.action());
                })
        );

        // Ensure state is actually changed
        assertFalse(ui.sidebar(SidebarUI.builder(COMPONENT_TITLE).build()));
        ui.drain(packet -> { throw new IllegalStateException(); });
    }

    @Test
    public void sidebarBuilderSet() {
        var ui = PlayerUI.newPlayerUI();
        assertTrue(ui.sidebar(SidebarUI.builder(COMPONENT_TITLE)
                .set(1, COMPONENT_LINE1) // Must also set line 0 to Component.empty()
                .build()));

        AtomicReference<String> objectiveName = new AtomicReference<>();
        AtomicReference<String> entityName = new AtomicReference<>();

        assertPackets(ui,
                packetCond(ScoreboardObjectivePacket.class, p -> {
                    assertEquals(ADD_OBJECTIVE, p.mode());
                    assertEquals(COMPONENT_TITLE, p.objectiveValue());
                    objectiveName.set(p.objectiveName());
                }),
                packetCond(DisplayScoreboardPacket.class, p -> {
                    assertEquals((byte) 1, p.position());
                    assertEquals(objectiveName.get(), p.scoreName());
                }),
                packetCond(TeamsPacket.class, p -> {
                    assertEquals(p.action().getClass(), TeamsPacket.CreateTeamAction.class);
                    if (p.action() instanceof TeamsPacket.CreateTeamAction createTeamAction) {
                        assertEquals(Component.empty(), createTeamAction.teamPrefix());
                        assertEquals(1, createTeamAction.entities().size());
                        entityName.set(createTeamAction.entities().iterator().next());
                    }
                }),
                packetCond(UpdateScorePacket.class, p -> {
                    assertEquals(objectiveName.get(), p.objectiveName());
                    assertEquals(entityName.get(), p.entityName());
                    assertEquals(UPDATE_SCORE, p.action());
                }),
                packetCond(TeamsPacket.class, p -> {
                    assertEquals(p.action().getClass(), TeamsPacket.CreateTeamAction.class);
                    if (p.action() instanceof TeamsPacket.CreateTeamAction createTeamAction) {
                        assertEquals(COMPONENT_LINE1, createTeamAction.teamPrefix());
                        assertEquals(1, createTeamAction.entities().size());
                        entityName.set(createTeamAction.entities().iterator().next());
                    }
                }),
                packetCond(UpdateScorePacket.class, p -> {
                    assertEquals(objectiveName.get(), p.objectiveName());
                    assertEquals(entityName.get(), p.entityName());
                    assertEquals(UPDATE_SCORE, p.action());
                })
        );

        // Ensure state is actually changed
        assertFalse(ui.sidebar(SidebarUI.builder(COMPONENT_TITLE)
                .set(1, COMPONENT_LINE1) // Must also set line 0 to Component.empty()
                .build()));
        ui.drain(packet -> { throw new IllegalStateException(); });
    }

    @SuppressWarnings("unchecked")
    private <T> Consumer<ServerPacket> packetCond(Class<T> clazz, Consumer<T> consumer) {
        return packet -> {
            if (packet.getClass() == clazz) {
                consumer.accept((T) packet);
            } else {
                throw new RuntimeException("Expected " + clazz.getClass().getName() + ", got " + packet.getClass().getName() + " instead");
            }
        };
    }

    private SidebarUI.Builder sidebar() {
        return SidebarUI.builder(COMPONENT_TITLE)
                .add(COMPONENT_LINE0)
                .add(COMPONENT_LINE1);
    }

    @SuppressWarnings("unchecked")
    private void assertPackets(PlayerUI ui, Consumer<?>... consumers) {
        AtomicInteger index = new AtomicInteger();
        ui.drain(packet -> {
            int i = index.getAndIncrement();
            if (i >= consumers.length) {
                throw new RuntimeException("Expected no more packets, but got " + packet.getClass().getName());
            } else {
                ((Consumer<ServerPacket>)consumers[i]).accept(packet);
            }
        });
        if (consumers.length > index.get()) {
            throw new RuntimeException("Expected packet, but no more packets were sent");
        }
    }

}
