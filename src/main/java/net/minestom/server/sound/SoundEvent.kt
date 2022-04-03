package net.minestom.server.sound

import net.kyori.adventure.key.Key
import net.minestom.server.registry.ProtocolObject
import net.kyori.adventure.sound.Sound
import net.minestom.server.sound.SoundEvents
import net.minestom.server.sound.SoundEvent
import net.minestom.server.sound.SoundEventImpl
import net.minestom.server.utils.NamespaceID

interface SoundEvent : ProtocolObject, Sound.Type, SoundEvents {
    override fun key(): Key {
        return super@ProtocolObject.key()
    }

    companion object {
        fun values(): Collection<SoundEvent?> {
            return SoundEventImpl.Companion.values()
        }

        fun fromNamespaceId(namespaceID: String): SoundEvent? {
            return SoundEventImpl.Companion.getSafe(namespaceID)
        }

        fun fromNamespaceId(namespaceID: NamespaceID): SoundEvent? {
            return fromNamespaceId(namespaceID.asString())
        }

        fun fromId(id: Int): SoundEvent? {
            return SoundEventImpl.Companion.getId(id)
        }
    }
}