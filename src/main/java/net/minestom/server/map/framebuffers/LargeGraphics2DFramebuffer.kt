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
 * [LargeFramebuffer] that embeds a [BufferedImage],
 * allowing for rendering directly via [Graphics2D] or its pixel array.
 */
class LargeGraphics2DFramebuffer(private val width: Int, private val height: Int) : LargeFramebuffer {
    val backingImage: BufferedImage
    val renderer: Graphics2D
    private val pixels: IntArray

    init {
        backingImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        renderer = backingImage.createGraphics()
        pixels = (backingImage.raster.dataBuffer as DataBufferInt).data
    }

    operator fun get(x: Int, z: Int): Int {
        return pixels[x + z * width] // stride is always the width of the image
    }

    operator fun set(x: Int, z: Int, rgb: Int): LargeGraphics2DFramebuffer {
        pixels[x + z * width] = rgb
        return this
    }

    override fun width(): Int {
        return width
    }

    override fun height(): Int {
        return height
    }

    override fun createSubView(left: Int, top: Int): Framebuffer {
        return LargeFramebufferDefaultView(this, left, top)
    }

    override fun getMapColor(x: Int, y: Int): Byte {
        return MapColors.Companion.closestColor(get(x, y)).getIndex()
    }
}