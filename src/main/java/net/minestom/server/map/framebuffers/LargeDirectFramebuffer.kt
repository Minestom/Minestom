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
 * [LargeFramebuffer] with direct access to the colors array.
 *
 *
 * This implementation does not throw errors when accessing out-of-bounds coordinates through sub-views, and will instead
 * use [MapColors.NONE]. This is only the case for sub-views, access through [.setMapColor]
 * and [.getMapColor] will throw an exception if out-of-bounds coordinates are inputted.
 */
class LargeDirectFramebuffer(private val width: Int, private val height: Int) : LargeFramebuffer {
    val colors: ByteArray

    /**
     * Creates a new [LargeDirectFramebuffer] with the desired size
     *
     * @param width
     * @param height
     */
    init {
        colors = ByteArray(width * height)
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

    fun setMapColor(x: Int, y: Int, color: Byte): LargeDirectFramebuffer {
        if (!bounds(x, y)) throw IndexOutOfBoundsException("Invalid x;y coordinate: $x;$y")
        colors[y * width + x] = color
        return this
    }

    override fun getMapColor(x: Int, y: Int): Byte {
        if (!bounds(x, y)) throw IndexOutOfBoundsException("Invalid x;y coordinate: $x;$y")
        return colors[y * width + x]
    }

    private fun bounds(x: Int, y: Int): Boolean {
        return x >= 0 && x < width && y >= 0 && y < height
    }
}