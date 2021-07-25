package net.minestom.server.map.framebuffers;

import net.minestom.server.map.Framebuffer;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.GLFW_NATIVE_CONTEXT_API;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_API;

/**
 * GLFW-based framebuffer.
 *
 * Due to its interfacing with OpenGL(-ES), extra care needs to be applied when using this framebuffer.
 * Rendering to this framebuffer should only be done via the thread on which the context is present.
 * To perform map conversion at the end of a frame, it is advised to use {@link #render(Runnable)} to render to the map.
 *
 * Use {@link #changeRenderingThreadToCurrent} in a thread to switch the thread on which to render.
 *
 * Use {@link #setupRenderLoop} with a callback to setup a task in the {@link net.minestom.server.timer.SchedulerManager}
 * to automatically render to the offscreen buffer on a specialized thread.
 *
 * GLFWFramebuffer does not provide guarantee that the result of {@link #toMapColors()} is synchronized with rendering, but
 * it will be updated after each frame rendered through {@link #render(Runnable)} or {@link #setupRenderLoop(long, java.time.temporal.TemporalUnit, Runnable)}.
 *
 * This framebuffer is meant to render to a single map (ie it is only compatible with 128x128 rendering)
 */
public class GLFWFramebuffer extends GLFWCapableBuffer implements Framebuffer {

    private final byte[] colors = new byte[WIDTH*HEIGHT];
    private final ByteBuffer pixels = BufferUtils.createByteBuffer(WIDTH*HEIGHT*4);

    public GLFWFramebuffer() {
        this(GLFW_NATIVE_CONTEXT_API, GLFW_OPENGL_API);
    }

    /**
     * Creates the framebuffer and initializes a new context
     */
    public GLFWFramebuffer(int apiContext, int clientAPI) {
        super(WIDTH, HEIGHT, apiContext, clientAPI);
    }

    @Override
    public byte[] toMapColors() {
        return colors;
    }
}
