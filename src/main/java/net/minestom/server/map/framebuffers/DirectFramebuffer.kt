package net.minestom.server.map.framebuffers

import net.minestom.server.MinecraftServer.Companion.exceptionManager
import net.minestom.server.map.Framebuffer
import net.minestom.server.map.framebuffers.DirectFramebuffer
import java.awt.image.BufferedImage
import java.awt.Graphics2D
import java.awt.image.DataBufferInt
import net.minestom.server.map.framebuffers.Graphics2DFramebuffer
import net.minestom.server.map.MapColors
import net.minestom.server.map.LargeFramebuffer
import net.minestom.server.map.framebuffers.LargeFramebufferDefaultView
import net.minestom.server.map.framebuffers.LargeDirectFramebuffer
import java.lang.IndexOutOfBoundsException
import net.minestom.server.map.framebuffers.LargeGraphics2DFramebuffer
import net.minestom.server.map.MapColors.Multiplier
import java.util.concurrent.ConcurrentHashMap
import net.minestom.server.map.MapColors.PreciseMapColor
import net.minestom.server.map.MapColors.ColorMappingStrategy
import java.lang.IllegalArgumentException
import java.lang.NumberFormatException
import net.minestom.server.MinecraftServer
import net.minestom.server.network.packet.server.play.MapDataPacket
import net.minestom.server.network.packet.server.play.MapDataPacket.ColorContent

/**
 * [Framebuffer] with direct access to the colors array
 */
class DirectFramebuffer : Framebuffer {
    /**
     * Mutable colors array
     *
     * @return
     */
    val colors = ByteArray(Framebuffer.Companion.WIDTH * Framebuffer.Companion.HEIGHT)
    operator fun get(x: Int, z: Int): Byte {
        return colors[index(x, z)]
    }

    operator fun set(x: Int, z: Int, color: Byte): DirectFramebuffer {
        colors[index(x, z)] = color
        return this
    }

    override fun toMapColors(): ByteArray {
        return colors
    }
}