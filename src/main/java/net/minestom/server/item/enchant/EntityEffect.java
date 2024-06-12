package net.minestom.server.item.enchant;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public non-sealed interface EntityEffect extends Enchantment.Effect {
    //todo all of these + attributes are considered location-based effects

    @NotNull BinaryTagSerializer<EntityEffect> NBT_TYPE = null; //todo

    record AllOf(@NotNull List<EntityEffect> effect) implements EntityEffect {
        public AllOf {
            effect = List.copyOf(effect);
        }
    }

    record ApplyPotionEffect(
            @NotNull ObjectSet<PotionEffect> toApply,
            @NotNull LevelBasedValue minDuration,
            @NotNull LevelBasedValue maxDuration,
            @NotNull LevelBasedValue minAmplifier,
            @NotNull LevelBasedValue maxAmplifier
    ) implements EntityEffect {

    }

    record DamageEntity(
            @NotNull DynamicRegistry.Key<DamageType> damageType,
            @NotNull LevelBasedValue minDamage,
            @NotNull LevelBasedValue maxDamage
    ) implements EntityEffect {

    }

    record DamageItem(@NotNull LevelBasedValue amount) implements EntityEffect {

    }

    record Explode(
            //todo
    ) implements EntityEffect {

    }

    record Ignite(@NotNull LevelBasedValue duration) implements EntityEffect {

    }

    record PlaySound(
            @NotNull SoundEvent sound,
            Object volume, // "A Float Provider between 0.00001 and 10.0 specifying the volume of the sound"
            Object pitch // "A Float Provider between 0.00001 and 2.0 specifying the pitch of the sound"
    ) implements EntityEffect {

    }

    record ReplaceBlock(
            Object blockState, // "A block state provider giving the block state to set"
            @NotNull Point offset,
            @Nullable Object predicate // "A World-generation style Block Predicate to used to determine if the block should be replaced"
    ) implements EntityEffect {

    }

    record ReplaceDisc(
            // todo
    ) implements EntityEffect {

    }

    record RunFunction(
            @NotNull String function
    ) implements EntityEffect {

    }

    record SetBlockProperties(
            //todo
    ) implements EntityEffect {

    }

    record SpawnParticles(
            //todo
    ) implements EntityEffect {

    }

    record SummonEntity(
            //todo
    ) implements EntityEffect {

    }

}
