package net.minestom.server.map.framebuffers;

import net.minestom.server.map.Framebuffer;
import net.minestom.server.map.LargeFramebuffer;

import static org.lwjgl.glfw.GLFW.GLFW_NATIVE_CONTEXT_API;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_API;

public class LargeGLFWFramebuffer extends GLFWCapableBuffer implements LargeFramebuffer {
    public LargeGLFWFramebuffer(int width, int height) {
        this(width, height, GLFW_NATIVE_CONTEXT_API, GLFW_OPENGL_API);
    }

    public LargeGLFWFramebuffer(int width, int height, int apiContext, int clientAPI) {
        super(width, height, apiContext, clientAPI);
    }

    @Override
    public Framebuffer createSubView(int left, int top) {
        return new LargeFramebufferDefaultView(this, left, top);
    }

    @Override
    public byte getMapColor(int x, int y) {
        return colors[Framebuffer.index(x, y, width())];
    }
}
