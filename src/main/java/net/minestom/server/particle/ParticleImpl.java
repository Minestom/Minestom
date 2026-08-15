package net.minestom.server.particle;

import net.kyori.adventure.key.Key;
import net.minestom.server.color.AlphaColor;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.UnknownNullability;

@SuppressWarnings("removal")
final class ParticleImpl {
    static final Registry<Particle> REGISTRY = RegistryData.createStaticRegistry(BuiltinRegistries.PARTICLE_TYPE,
            (namespace, properties) -> defaultParticle(Key.key(namespace), properties.getInt("id")));

    @SuppressWarnings("unchecked")
    static <P extends Particle> @UnknownNullability P get(RegistryKey<P> key) {
        return (P) REGISTRY.get(key.key());
    }

    @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
    static <P extends Particle> @UnknownNullability P get(Key key) {
        return (P) REGISTRY.get(key);
    }

    private static Particle defaultParticle(Key key, int id) {
        return switch (key.value()) {
            case "block" -> new Particle.Block(key, id, Block.STONE);
            case "block_marker" -> new Particle.BlockMarker(key, id, Block.STONE);
            case "falling_dust" -> new Particle.FallingDust(key, id, Block.STONE);
            case "dust_pillar" -> new Particle.DustPillar(key, id, Block.STONE);
            case "dust" -> new Particle.Dust(key, id, Color.WHITE, 1);
            case "dust_color_transition" ->
                    new Particle.DustColorTransition(key, id, Color.WHITE, Color.WHITE, 1);
            case "sculk_charge" -> new Particle.SculkCharge(key, id, 0);
            case "item" -> new Particle.Item(key, id, ItemStack.AIR);
            case "vibration" ->
                    new Particle.Vibration(key, id, Particle.Vibration.SourceType.BLOCK, Vec.ZERO, 0, 0, 0);
            case "shriek" -> new Particle.Shriek(key, id, 0);
            case "entity_effect" -> new Particle.EntityEffect(key, id, AlphaColor.WHITE);
            case "trail" -> new Particle.Trail(key, id, Vec.ZERO, Color.WHITE, 0);
            case "block_crumble" -> new Particle.BlockCrumble(key, id, Block.STONE);
            case "tinted_leaves" -> new Particle.TintedLeaves(key, id, AlphaColor.WHITE);
            case "dragon_breath" -> new Particle.DragonBreath(key, id, 1);
            case "effect" -> new Particle.Effect(key, id, Color.WHITE, 1);
            case "flash" -> new Particle.Flash(key, id, AlphaColor.WHITE);
            case "instant_effect" -> new Particle.InstantEffect(key, id, Color.WHITE, 1);
            case "geyser" -> new Particle.Geyser(key, id, 1);
            case "geyser_base" -> new Particle.GeyserBase(key, id, 1, 0);
            case "geyser_plume" -> new Particle.GeyserPlume(key, id, 1);
            case "geyser_poof" -> new Particle.GeyserPoof(key, id, 1, 0);
            default -> new Particle.Simple(key, id);
        };
    }

    private ParticleImpl() {
    }
}
