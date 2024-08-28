package net.minestom.scratch.entity;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.EntityEffectPacket;
import net.minestom.server.network.packet.server.play.RemoveEntityEffectPacket;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.Potion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class PotionHolder {
    private final int entityId;
    private final Consumer<ServerPacket.Play> consumer;
    private final Map<CustomPotionEffect, Long> effects = new HashMap<>();

    private long counter;

    public PotionHolder(int entityId, Consumer<ServerPacket.Play> consumer) {
        this.entityId = entityId;
        this.consumer = consumer;
    }

    public void apply(CustomPotionEffect effect) {
        System.out.println("apply effect: " + effect);
        final CustomPotionEffect.Settings settings = effect.settings();
        byte flags = 0;
        if (settings.isAmbient()) flags |= Potion.AMBIENT_FLAG;
        if (settings.showParticles()) flags |= Potion.PARTICLES_FLAG;
        if (settings.showIcon()) flags |= Potion.ICON_FLAG;
        var potion = new Potion(effect.id(), (byte) effect.amplifier(), effect.duration(), flags);
        this.effects.put(effect, System.currentTimeMillis());
        this.consumer.accept(new EntityEffectPacket(entityId, potion));
    }

    public void updateEffects() {
        var effects = this.effects;
        if (effects.isEmpty()) return;
        Set<CustomPotionEffect> toRemove = new HashSet<>();
        for (Map.Entry<CustomPotionEffect, Long> entry : effects.entrySet()) {
            final CustomPotionEffect effect = entry.getKey();
            final Long start = entry.getValue();
            // Handle duration
            if (effect.duration() != Potion.INFINITE_DURATION) {
                var elapsed = counter - start;
                if (elapsed >= effect.duration()) {
                    toRemove.add(effect);
                    consumer.accept(new RemoveEntityEffectPacket(entityId, effect.id()));
                }
            }
            // TODO handle effect
        }
        toRemove.forEach(effects::remove);
        this.counter++;
    }
}
