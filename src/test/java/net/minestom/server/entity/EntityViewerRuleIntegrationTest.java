package net.minestom.server.entity;

import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
class EntityViewerRuleIntegrationTest {

    @Test
    void viewableRule(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 0));
        p1.updateViewableRule(p -> p.getEntityId() == p1.getEntityId() + 1);

        var p2 = env.createPlayer(instance, new Pos(0, 42, 0));

        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        p1.updateViewableRule(player -> false);

        assertEquals(0, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());
    }

    @Test
    void viewableRuleUpdate(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 0));

        AtomicBoolean enabled = new AtomicBoolean(false);
        p1.updateViewableRule(p -> enabled.get());

        var p2 = env.createPlayer(instance, new Pos(0, 42, 0));
        assertEquals(0, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        enabled.set(true);
        p1.updateViewableRule();
        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());
    }

    @Test
    void viewableRuleDouble(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 0));
        var p2 = env.createPlayer(instance, new Pos(0, 42, 0));
        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        AtomicBoolean enabled1 = new AtomicBoolean(false);
        AtomicBoolean enabled2 = new AtomicBoolean(false);

        p1.updateViewableRule(p -> enabled1.get());
        p2.updateViewableRule(p -> enabled2.get());
        assertEquals(0, p1.getViewers().size());
        assertEquals(0, p2.getViewers().size());

        enabled1.set(true);
        p1.updateViewableRule();
        assertEquals(1, p1.getViewers().size());
        assertEquals(0, p2.getViewers().size());

        enabled2.set(true);
        p2.updateViewableRule();
        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        enabled1.set(false);
        p1.updateViewableRule();
        assertEquals(0, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());
    }

    @Test
    void viewerRule(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 0));
        p1.updateViewerRule(e -> e.getEntityId() == p1.getEntityId() + 1);

        var p2 = env.createPlayer(instance, new Pos(0, 42, 0));

        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        p1.updateViewerRule(player -> false);

        assertEquals(1, p1.getViewers().size());
        assertEquals(0, p2.getViewers().size());
    }

    @Test
    void viewerRuleUpdate(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 0));
        AtomicBoolean enabled = new AtomicBoolean(false);
        p1.updateViewerRule(e -> enabled.get());

        var p2 = env.createPlayer(instance, new Pos(0, 42, 0));
        assertEquals(1, p1.getViewers().size());
        assertEquals(0, p2.getViewers().size());

        enabled.set(true);
        p1.updateViewerRule();
        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());
    }

    @Test
    void viewerRuleDouble(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 0));
        var p2 = env.createPlayer(instance, new Pos(0, 42, 0));
        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        AtomicBoolean enabled1 = new AtomicBoolean(false);
        AtomicBoolean enabled2 = new AtomicBoolean(false);

        p1.updateViewerRule(e -> enabled1.get());
        p2.updateViewerRule(e -> enabled2.get());
        assertEquals(0, p1.getViewers().size());
        assertEquals(0, p2.getViewers().size());

        enabled1.set(true);
        p1.updateViewerRule();
        assertEquals(0, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        enabled2.set(true);
        p2.updateViewerRule();
        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        enabled1.set(false);
        p1.updateViewerRule();
        assertEquals(1, p1.getViewers().size());
        assertEquals(0, p2.getViewers().size());
    }
}
