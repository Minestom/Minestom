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
 * Framebuffer to render to a map
 */
interface Framebuffer {
    fun toMapColors(): ByteArray

    @JvmOverloads
    fun preparePacket(
        mapId: Int,
        minX: Int = 0,
        minY: Int = 0,
        width: Int = WIDTH,
        height: Int = HEIGHT
    ): MapDataPacket? {
        val colors: ByteArray
        if (minX == 0 && minY == 0 && width == WIDTH && height == HEIGHT) {
            colors = toMapColors()
        } else {
            colors = ByteArray(width * height)
            val mapColors = toMapColors()
            for (y in minY until Math.min(HEIGHT, minY + height)) {
                for (x in minX until Math.min(WIDTH, minX + width)) {
                    val color = mapColors[index(x, y, WIDTH)]
                    colors[index(x - minX, y - minY, width)] = color
                }
            }
        }
        return MapDataPacket(
            mapId, 0.toByte(), false,
            false, List.of(),
            ColorContent(
                width.toByte(), height.toByte(), minX.toByte(), minY.toByte(),
                colors
            )
        )
    }

    companion object {
        @JvmOverloads
        fun index(x: Int, z: Int, stride: Int = WIDTH): Int {
            return z * stride + x
        }

        const val WIDTH = 128
        const val HEIGHT = 128
    }
}