package net.minestom.server.map.framebuffers;

import net.minestom.server.MinecraftServer;
import net.minestom.server.map.Framebuffer;
import net.minestom.server.map.MapColors;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.thread.ThreadBindingExecutor;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.temporal.TemporalUnit;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public abstract class GLFWCapableBuffer {

    protected final byte[] colors;
    private final ByteBuffer pixels;
    private final long glfwWindow;
    private final int width;
    private final int height;
    private final ByteBuffer colorsBuffer;
    private boolean onlyMapColors;

    private static ThreadBindingExecutor threadBindingPool;

    protected GLFWCapableBuffer(int width, int height) {
        this(width, height, GLFW_NATIVE_CONTEXT_API, GLFW_OPENGL_API);
    }

    /**
     * Creates the framebuffer and initializes a new context
     */
    protected GLFWCapableBuffer(int width, int height, int apiContext, int clientAPI) {
        this.width = width;
        this.height = height;
        this.colors = new byte[width*height];
        colorsBuffer = BufferUtils.createByteBuffer(width*height);
        this.pixels = BufferUtils.createByteBuffer(width*height*4);
        if(!glfwInit()) {
            throw new RuntimeException("Failed to init GLFW");
        }

        GLFWErrorCallback.createPrint().set();
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        glfwWindowHint(GLFW_CONTEXT_CREATION_API, apiContext);
        glfwWindowHint(GLFW_CLIENT_API, clientAPI);

        this.glfwWindow = glfwCreateWindow(width, height, "", 0L, 0L);
        if(glfwWindow == 0L) {
            try(var stack = MemoryStack.stackPush()) {
                PointerBuffer desc = stack.mallocPointer(1);
                int errcode = glfwGetError(desc);
                throw new RuntimeException("("+errcode+") Failed to create GLFW Window.");
            }
        }

        synchronized(GLFWCapableBuffer.class) {
            if(threadBindingPool == null) {
                threadBindingPool = new ThreadBindingExecutor(MinecraftServer.THREAD_COUNT_SCHEDULER, MinecraftServer.THREAD_NAME_SCHEDULER);
            }
        }
    }

    public GLFWCapableBuffer unbindContextFromThread() {
        glfwMakeContextCurrent(0L);
        return this;
    }

    public void changeRenderingThreadToCurrent() {
        glfwMakeContextCurrent(glfwWindow);
        GL.createCapabilities();
    }

    public Task setupRenderLoop(long period, TemporalUnit unit, Runnable rendering) {
        return setupRenderLoop(Duration.of(period, unit), rendering);
    }

    public Task setupRenderLoop(Duration period, Runnable rendering) {
        return MinecraftServer.getSchedulerManager()
                .buildTask(new Runnable() {
                    private boolean first = true;
                    private final Runnable subAction = () -> {
                        if(first) {
                            changeRenderingThreadToCurrent();
                            first = false;
                        }
                        render(rendering);
                    };

                    @Override
                    public void run() {
                        threadBindingPool.execute(subAction);
                    }
                })
                .repeat(period)
                .schedule();
    }

    public void render(Runnable rendering) {
        rendering.run();
        glfwSwapBuffers(glfwWindow);
        prepareMapColors();
    }

    /**
     * Called in render after glFlush to read the pixel buffer contents and convert it to map colors.
     * Only call if you do not use {@link #render(Runnable)} nor {@link #setupRenderLoop}
     */
    public void prepareMapColors() {
        if(onlyMapColors) {
            colorsBuffer.rewind();
            glReadPixels(0, 0, width, height, GL_RED, GL_UNSIGNED_BYTE, colorsBuffer);
            colorsBuffer.get(colors);
        } else {
            glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int i = Framebuffer.index(x, y, width)*4;
                    int red = pixels.get(i) & 0xFF;
                    int green = pixels.get(i+1) & 0xFF;
                    int blue = pixels.get(i+2) & 0xFF;
                    int alpha = pixels.get(i+3) & 0xFF;
                    int argb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    colors[Framebuffer.index(x, y, width)] = MapColors.closestColor(argb).getIndex();
                }
            }
        }
    }

    public void cleanup() {
        glfwTerminate();
    }

    public long getGLFWWindow() {
        return glfwWindow;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /**
     * Tells this buffer that the **RED** channel contains the index of the map color to use.
     *
     * This allows for optimizations and fast rendering (because there is no need for a conversion)
     */
    public void useMapColors() {
        onlyMapColors = true;
    }

    /**
     * Opposite to {@link #useMapColors()}
     */
    public void useRGB() {
        onlyMapColors = false;
    }
}
