package net.minestom.server.entity.metadata.item

import net.minestom.server.entity.Entity
import net.minestom.server.entity.Metadata
import net.minestom.server.item.Material

class ThrownPotionMeta(entity: Entity, metadata: Metadata) : ItemContainingMeta(entity, metadata, Material.AIR) {
    companion object {
        val OFFSET: Byte = ItemContainingMeta.Companion.MAX_OFFSET
        val MAX_OFFSET = (OFFSET + 0).toByte()
    }
}