package net.minestom.server.map.framebuffers;

import net.minestom.server.MinecraftServer;
import net.minestom.server.map.Framebuffer;
import net.minestom.server.map.MapColors;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.time.TimeUnit;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.glfw.GLFW.*;

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
 * it will be updated after each frame rendered through {@link #render(Runnable)} or {@link #setupRenderLoop(long, TimeUnit, Runnable)}.
 */
public class GLFWFramebuffer implements Framebuffer {

    private final byte[] colors = new byte[WIDTH*HEIGHT];
    private final ByteBuffer pixels = BufferUtils.createByteBuffer(WIDTH*HEIGHT*4);
    private final long glfwWindow;

    public GLFWFramebuffer() {
        this(GLFW_NATIVE_CONTEXT_API, GLFW_OPENGL_API);
    }

    /**
     * Creates the framebuffer and initializes a new EGL context
     */
    public GLFWFramebuffer(int apiContext, int clientAPI) {
        if(!glfwInit()) {
            throw new RuntimeException("Failed to init GLFW");
        }

        GLFWErrorCallback.createPrint().set();
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        glfwWindowHint(GLFW_CONTEXT_CREATION_API, apiContext);
        glfwWindowHint(GLFW_CLIENT_API, clientAPI);

        this.glfwWindow = glfwCreateWindow(WIDTH, HEIGHT, "", 0L, 0L);
        if(glfwWindow == 0L) {
            try(var stack = MemoryStack.stackPush()) {
                PointerBuffer desc = stack.mallocPointer(1);
                int errcode = glfwGetError(desc);
                throw new RuntimeException("("+errcode+") Failed to create GLFW Window.");
            }
        }
    }

    public GLFWFramebuffer unbindContextFromThread() {
        glfwMakeContextCurrent(0L);
        return this;
    }

    public void changeRenderingThreadToCurrent() {
        glfwMakeContextCurrent(glfwWindow);
        GL.createCapabilities();
    }

    public Task setupRenderLoop(long period, TimeUnit unit, Runnable rendering) {
        return MinecraftServer.getSchedulerManager()
                .buildTask(new Runnable() {
                    private boolean first = true;

                    @Override
                    public void run() {
                        if(first) {
                            changeRenderingThreadToCurrent();
                            first = false;
                        }
                        render(rendering);
                    }
                })
                .repeat(period, unit)
                .schedule();
    }

    public void render(Runnable rendering) {
        rendering.run();
        glfwSwapBuffers(glfwWindow);
        prepareMapColors();
    }

    /**
     * Called in render after glFlush to read the pixel buffer contents and convert it to map colors.
     * Only call if you do not use {@link #render(Runnable)} nor {@link #setupRenderLoop(long, TimeUnit, Runnable)}
     */
    public void prepareMapColors() {
        glReadPixels(0, 0, WIDTH, HEIGHT, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int i = Framebuffer.index(x, y)*4;
                int red = pixels.get(i) & 0xFF;
                int green = pixels.get(i+1) & 0xFF;
                int blue = pixels.get(i+2) & 0xFF;
                int alpha = pixels.get(i+3) & 0xFF;
                int argb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                colors[Framebuffer.index(x, y)] = MapColors.closestColor(argb).getIndex();
            }
        }
    }

    public void cleanup() {
        glfwTerminate();
    }

    public long getGLFWWindow() {
        return glfwWindow;
    }

    @Override
    public byte[] toMapColors() {
        return colors;
    }
}
