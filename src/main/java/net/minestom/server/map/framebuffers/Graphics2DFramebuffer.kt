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
 * [Framebuffer] that embeds a BufferedImage, allowing for rendering directly via Graphics2D or its pixel array.
 */
class Graphics2DFramebuffer : Framebuffer {
    private val colors = ByteArray(Framebuffer.Companion.WIDTH * Framebuffer.Companion.HEIGHT)
    val backingImage =
        BufferedImage(Framebuffer.Companion.WIDTH, Framebuffer.Companion.HEIGHT, BufferedImage.TYPE_INT_RGB)
    val renderer: Graphics2D
    private val pixels: IntArray

    init {
        renderer = backingImage.createGraphics()
        pixels = (backingImage.raster.dataBuffer as DataBufferInt).data
    }

    operator fun get(x: Int, z: Int): Int {
        return pixels[x + z * Framebuffer.Companion.WIDTH] // stride is always the width of the image
    }

    operator fun set(x: Int, z: Int, rgb: Int): Graphics2DFramebuffer {
        pixels[x + z * Framebuffer.Companion.WIDTH] = rgb
        return this
    }

    override fun toMapColors(): ByteArray {
        // TODO: update subparts only
        for (x in 0..127) {
            for (z in 0..127) {
                colors[index(x, z)] = MapColors.Companion.closestColor(get(x, z)).getIndex()
            }
        }
        return colors
    }
}