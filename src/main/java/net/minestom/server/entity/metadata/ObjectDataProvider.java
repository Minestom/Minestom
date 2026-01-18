package net.minestom.server.entity.metadata;

import net.minestom.server.entity.metadata.item.FireballMeta;
import net.minestom.server.entity.metadata.item.ItemEntityMeta;
import net.minestom.server.entity.metadata.item.SmallFireballMeta;
import net.minestom.server.entity.metadata.other.*;
import net.minestom.server.entity.metadata.projectile.*;

// https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Object_Data
public sealed interface ObjectDataProvider permits FireballMeta, ItemEntityMeta, SmallFireballMeta, FallingBlockMeta, FishingHookMeta, HangingMeta, LlamaSpitMeta, ShulkerBulletMeta, AbstractWindChargeMeta, ArrowMeta, DragonFireballMeta, SpectralArrowMeta, WitherSkullMeta {

    int getObjectData();

    boolean requiresVelocityPacketAtSpawn();

}
