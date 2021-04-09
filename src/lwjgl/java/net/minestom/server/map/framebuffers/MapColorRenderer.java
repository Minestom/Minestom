package net.minestom.server.map.framebuffers;

import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL30.*;

/**
 * Helper class designed to help OpenGL users to convert their RGB values to map colors inside a post processing pass
 * with a shader provided by Minestom.
 *
 * When rendering to a {@link GLFWFramebuffer} or a {@link LargeGLFWFramebuffer}, wrap your rendering in a MapColorRenderer to render to the GLFW with map colors.
 *
 * {@link MapColorRenderer} sets up an OpenGL framebuffer with the size of the underlying framebuffer and renders to it.
 * The initialization of the framebuffer is done in the constructor.
 * Therefore, the constructor call should be done inside the thread linked to the OpenGL context. The context can
 * be moved through {@link GLFWCapableBuffer#changeRenderingThreadToCurrent()} and {@link GLFWCapableBuffer#unbindContextFromThread()}
 *
 * <hr>
 * Resources created in constructor are:
 * <ul>
 *     <li>Framebuffer</li>
 *     <li>Color texture (if default fbo initialization chosen)</li>
 *     <li>Depth24 Stencil8 render buffer (if default fbo initialization chosen)</li>
 *     <li>Post processing shader program</li>
 *     <li>Palette texture</li>
 *     <li>Screen quad VAO</li>
 *     <li>Screen quad index buffer</li>
 * </ul>
 *
 * The constructor also puts the given buffer in map color mode.
 */
public class MapColorRenderer implements Runnable {

    private final int fboID;
    private final GLFWCapableBuffer framebuffer;
    private final Runnable renderCode;
    private final int colorTextureID;
    private final int width;
    private final int height;
    private final int renderShader;
    private final int screenQuadIndices;
    private int paletteTexture;
    private float paletteSize;
    private final int screenQuadVAO;

    public MapColorRenderer(GLFWCapableBuffer framebuffer, Runnable renderCode) {
        this(framebuffer, renderCode, MapColorRenderer.defaultFramebuffer(framebuffer.width(), framebuffer.height()));
    }

    public MapColorRenderer(GLFWCapableBuffer framebuffer, Runnable renderCode, FboInitialization fboInitialization) {
        this.framebuffer = framebuffer;
        this.framebuffer.useMapColors();

        this.renderCode = renderCode;
        this.width = framebuffer.width();
        this.height = framebuffer.height();

        this.fboID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fboID);
        this.colorTextureID = fboInitialization.initFbo(fboID);

        if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer is not complete!");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // create post-process shader
        this.renderShader = glCreateProgram();
        int vertexShader = createShader("/shaders/mapcolorconvert.vertex.glsl", GL_VERTEX_SHADER);
        int fragmentShader = createShader("/shaders/mapcolorconvert.fragment.glsl", GL_FRAGMENT_SHADER);
        glAttachShader(renderShader, vertexShader);
        glAttachShader(renderShader, fragmentShader);
        glLinkProgram(renderShader);
        if(glGetProgrami(renderShader, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Link error: "+glGetProgramInfoLog(renderShader));
        }

        loadPalette("palette");

        // create screen quad VAO
        screenQuadVAO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, screenQuadVAO);
        glBufferData(GL_ARRAY_BUFFER, new float[] {
                -1f, -1f,
                1f, -1f,
                1f, 1f,
                -1f, 1f
        }, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        screenQuadIndices = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, screenQuadIndices);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, new int[] {0,1,2, 2,3,0}, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        int paletteSizeUniform = glGetUniformLocation(renderShader, "paletteSize");
        int paletteUniform = glGetUniformLocation(renderShader, "palette");
        int frameUniform = glGetUniformLocation(renderShader, "frame");

        glUseProgram(renderShader); {
            glUniform1i(frameUniform, 0); // texture unit 0
            glUniform1i(paletteUniform, 1); // texture unit 1
            glUniform1f(paletteSizeUniform, paletteSize);
        }
        glUseProgram(0);
    }

    private static FboInitialization defaultFramebuffer(int width, int height) {
        return fboId -> defaultFramebufferInit(fboId, width, height);
    }

    private static int defaultFramebufferInit(int fbo, int width, int height) {
        // color
        int colorTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, colorTexture);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0L);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // attach to framebuffer
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTexture, 0);
        glBindTexture(GL_TEXTURE_2D, 0);

        // depth
        int depthStencilBuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthStencilBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthStencilBuffer);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
        return colorTexture;
    }

    @Override
    public void run() {
        glViewport(0, 0, width, height);
        // run user code inside of framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, fboID);
        renderCode.run();

        // run post processing to display on screen
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClearColor(0f, 0f, 0f, 1f); // 0 on RED channel makes maps use NONE
        glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, colorTextureID);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, paletteTexture);

        glUseProgram(renderShader); {
            // render post processing quad
            glBindBuffer(GL_ARRAY_BUFFER, screenQuadVAO);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 2*4, 0); // position

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, screenQuadIndices);
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
        }

        glUseProgram(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /**
     * Frees OpenGL resources used by this renderer.
     * You should NOT render with this renderer after this call.
     */
    public void cleanupResources() {
        glDeleteFramebuffers(fboID);
        glDeleteProgram(renderShader);
        glDeleteTextures(paletteTexture);
        // TODO: more cleanup
    }

    private void loadPalette(String filename) {
        int tex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, tex);
        BufferedImage image;
        try {
            image = ImageIO.read(MapColorRenderer.class.getResourceAsStream("/textures/"+filename+".png"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Missing image "+filename, e);
        }
        ByteBuffer pixels = BufferUtils.createByteBuffer(image.getWidth()*image.getHeight()*4);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                pixels.put((byte) red);
                pixels.put((byte) green);
                pixels.put((byte) blue);
                pixels.put((byte) alpha);
            }
        }
        pixels.flip();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        // closest neighbor required here, as pixels can have very different rgb values, and interpolation will break palette lookup
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);

        this.paletteTexture = tex;
        this.paletteSize = image.getWidth();
    }

    private static int createShader(String filename, int type) {
        int shader = glCreateShader(type);
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(MapColorRenderer.class.getResourceAsStream(filename)))) {
            String source = reader.lines().collect(Collectors.joining("\n"));
            glShaderSource(shader, source);
            glCompileShader(shader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return shader;
    }

    @FunctionalInterface
    public interface FboInitialization {

        /**
         * Initializes the given framebuffer
         * @param fboId
         * @return the texture ID of the color texture, used for post processing.
         */
        int initFbo(int fboId);
    }

}



