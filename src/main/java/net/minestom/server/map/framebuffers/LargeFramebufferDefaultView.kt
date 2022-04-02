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

class LargeFramebufferDefaultView(private val parent: LargeFramebuffer, private val x: Int, private val y: Int) :
    Framebuffer {
    private val colors = ByteArray(Framebuffer.Companion.WIDTH * Framebuffer.Companion.HEIGHT)
    private fun bounds(x: Int, y: Int): Boolean {
        return x >= 0 && x < parent.width() && y >= 0 && y < parent.height()
    }

    private fun colorOrNone(x: Int, y: Int): Byte {
        return if (!bounds(x, y)) MapColors.NONE.baseColor() else parent.getMapColor(x, y)
    }

    override fun toMapColors(): ByteArray {
        for (y in 0 until Framebuffer.Companion.HEIGHT) {
            for (x in 0 until Framebuffer.Companion.WIDTH) {
                colors[index(x, y)] = colorOrNone(x + this.x, y + this.y)
            }
        }
        return colors
    }
}