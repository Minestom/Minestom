package net.minestom.server.sound

import net.minestom.server.registry.ProtocolObject
import net.kyori.adventure.sound.Sound
import net.minestom.server.registry.Registry
import net.minestom.server.sound.SoundEvents
import net.minestom.server.sound.SoundEvent
import net.minestom.server.sound.SoundEventImpl
import net.minestom.server.utils.NamespaceID

internal class SoundEventImpl : SoundEvent {
    override fun toString(): String {
        return name()
    }

    companion object {
        private val CONTAINER = Registry.createContainer<SoundEvent?>(
            Registry.Resource.SOUNDS
        ) { namespace: String?, properties: Registry.Properties ->
            SoundEventImpl(
                NamespaceID.from(
                    namespace!!
                ), properties.getInt("id")
            )
        }

        operator fun get(namespace: String): SoundEvent? {
            return CONTAINER[namespace]
        }

        fun getSafe(namespace: String): SoundEvent? {
            return CONTAINER.getSafe(namespace)
        }

        fun getId(id: Int): SoundEvent? {
            return CONTAINER.getId(id)
        }

        fun values(): Collection<SoundEvent?> {
            return CONTAINER.values()
        }
    }
}