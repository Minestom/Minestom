package net.minestom.demo.largeframebuffers;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GLUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL20.*;

public final  class OpenGLRendering {

    private static int vbo;
    private static int indexBuffer;
    private static final int VERTEX_SIZE = 5*4; // position + tex

    // array of vertices (order: X,Y,Z, Tex U, Tex V)
    private static float[] vertices = {
        // front face
        -1f, -1f, -1f, 0, 0,
        1f, -1f, -1f, 1, 0,
        1f, 1f, -1f, 1, 1,
        -1f, 1f, -1f, 0, 1,

        // back face
        -1f, -1f, 1f, 0, 1,
        1f, -1f, 1f, 1, 1,
        1f, 1f, 1f, 1, 0,
        -1f, 1f, 1f, 0, 0,
    };

    private static int[] indices = {
            // south face
            0,1,2,
            2,3,0,

            // north face
            4,5,6,
            6,7,4,

            // west face
            0,4,7,
            7,3,0,

            // east face
            1,5,6,
            6,2,1,

            // top face
            3, 2, 6,
            6, 7, 3
    };
    private static int renderShader;
    private static Matrix4f projectionMatrix;
    private static Matrix4f viewMatrix;
    private static Matrix4f modelMatrix;
    private static int projectionUniform;
    private static int viewUniform;
    private static int modelUniform;
    private static int boxTexture;

    static void init() {
        GLUtil.setupDebugMessageCallback();

        boxTexture = loadTexture("box");

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        indexBuffer = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // prepare matrices and shader
        renderShader = glCreateProgram();
        projectionMatrix = new Matrix4f().setPerspective((float) (Math.PI/4f), 1f, 0.001f, 100f);
        viewMatrix = new Matrix4f().setLookAt(5f, 5f, 5f, 0, 0, 0, 0, -1, 0);
        modelMatrix = new Matrix4f().identity();
        int vertexShader = createShader("/shaders/vertex.glsl", GL_VERTEX_SHADER);
        int fragmentShader = createShader("/shaders/fragment.glsl", GL_FRAGMENT_SHADER);
        glAttachShader(renderShader, vertexShader);
        glAttachShader(renderShader, fragmentShader);
        glLinkProgram(renderShader);
        if(glGetProgrami(renderShader, GL_LINK_STATUS) == 0) {
            System.err.println("Link error: "+glGetProgramInfoLog(renderShader));
        }

        projectionUniform = glGetUniformLocation(renderShader, "projection");
        viewUniform = glGetUniformLocation(renderShader, "view");
        modelUniform = glGetUniformLocation(renderShader, "model");
        int boxUniform = glGetUniformLocation(renderShader, "box");

        glUseProgram(renderShader); {
            uploadMatrix(projectionUniform, projectionMatrix);
            uploadMatrix(viewUniform, viewMatrix);

            glUniform1i(boxUniform, 0); // texture unit 0
        }
        glUseProgram(0);
    }

    private static int loadTexture(String filename) {
        int tex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, tex);
        BufferedImage image;
        try {
            image = ImageIO.read(OpenGLRendering.class.getResourceAsStream("/textures/"+filename+".png"));
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

        return tex;
    }

    private static void uploadMatrix(int uniform, Matrix4f matrix) {
        float[] values = new float[4*4];
        matrix.get(values);
        glUniformMatrix4fv(uniform, false, values);
    }

    private static int createShader(String filename, int type) {
        int shader = glCreateShader(type);
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(OpenGLRendering.class.getResourceAsStream(filename)))) {
            String source = reader.lines().collect(Collectors.joining("\n"));
            glShaderSource(shader, source);
            glCompileShader(shader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return shader;
    }

    private static long lastTime = System.currentTimeMillis();

    private static int frame = 0;

    static void render() {
        if(frame % 100 == 0) {
            long time = System.currentTimeMillis();
            long dt = time-lastTime;
            System.out.println(">> Render time for 100 frames: "+dt);
            System.out.println(">> Average time per frame: "+(dt/100.0));
            System.out.println(">> Average FPS: "+(1000.0/(dt/100.0)));
            lastTime = time;
        }
        frame++;


        glClearColor(0f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glEnable(GL_DEPTH_TEST);

        modelMatrix.rotateY((float) (Math.PI/60f));

        glUseProgram(renderShader); {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, boxTexture);

            uploadMatrix(modelUniform, modelMatrix);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, VERTEX_SIZE, 0); // position
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, VERTEX_SIZE, 3*4); // color

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
            glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
        }
        glUseProgram(0);
    }

}
