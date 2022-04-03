package net.minestom.server.map

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
import java.util.List

/**
 * Framebuffer that is meant to be split in sub-framebuffers.
 * Contrary to [Framebuffer], LargeFramebuffer supports sizes over 128x128 pixels.
 */
interface LargeFramebuffer {
    fun width(): Int
    fun height(): Int

    /**
     * Returns a new [Framebuffer] that represent a 128x128 sub-view of this framebuffer.
     * Implementations are free (but not guaranteed) to throw exceptions if left &amp; top produces out-of-bounds coordinates.
     *
     * @param left
     * @param top
     * @return the sub-view [Framebuffer]
     */
    fun createSubView(left: Int, top: Int): Framebuffer
    fun getMapColor(x: Int, y: Int): Byte

    /**
     * Prepares the packet to render a 128x128 sub view of this framebuffer
     */
    fun preparePacket(mapId: Int, left: Int, top: Int): MapDataPacket? {
        val colors = ByteArray(Framebuffer.Companion.WIDTH * Framebuffer.Companion.WIDTH)
        val width = Math.min(width(), left + Framebuffer.Companion.WIDTH) - left
        val height = Math.min(height(), top + Framebuffer.Companion.HEIGHT) - top
        for (y in top until height) {
            for (x in left until width) {
                val color = getMapColor(x, y)
                colors[index(x - left, y - top)] = color
            }
        }
        return MapDataPacket(
            mapId, 0.toByte(), false,
            false, List.of(),
            ColorContent(
                width.toByte(), height.toByte(), 0.toByte(), 0.toByte(),
                colors
            )
        )
    }
}