package fr.themode.demo.map;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.MapMeta;
import net.minestom.server.map.framebuffers.GLFWFramebuffer;
import net.minestom.server.map.framebuffers.Graphics2DFramebuffer;
import net.minestom.server.network.packet.server.play.MapDataPacket;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.time.TimeUnit;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class MapAnimationDemo {

    public static final int MAP_ID = 1;
    public static final int OPENGL_MAP_ID = 2;
    public static final int OPENGL2_MAP_ID = 3;

    private static final Graphics2DFramebuffer framebuffer = new Graphics2DFramebuffer();
    private static final GLFWFramebuffer glfwFramebuffer = new GLFWFramebuffer();
    private static final GLFWFramebuffer glfwFramebuffer2 = new GLFWFramebuffer();

    public static void init() {
        SchedulerManager scheduler = MinecraftServer.getSchedulerManager();
        scheduler.buildTask(MapAnimationDemo::tick).repeat(16, TimeUnit.MILLISECOND).schedule();

        MinecraftServer.getConnectionManager().addPlayerInitialization(player -> {
            player.addEventCallback(PlayerSpawnEvent.class, event -> {
                ItemStack map = new ItemStack(Material.FILLED_MAP, (byte) 1);
                map.setItemMeta(new MapMeta(MAP_ID));
                player.getInventory().addItemStack(map);

                ItemStack map2 = new ItemStack(Material.FILLED_MAP, (byte) 1);
                map2.setItemMeta(new MapMeta(OPENGL_MAP_ID));
                player.getInventory().addItemStack(map2);

                ItemStack map3 = new ItemStack(Material.FILLED_MAP, (byte) 1);
                map3.setItemMeta(new MapMeta(OPENGL2_MAP_ID));
                player.getInventory().addItemStack(map3);
            });
        });

        glfwFramebuffer.setupRenderLoop(16, TimeUnit.MILLISECOND, () -> {
            glClearColor(0f, 0f, 0f, 1f);
            glClear(GL_COLOR_BUFFER_BIT);

            glBegin(GL_TRIANGLES);

            glVertex2f(0, -0.75f);
            glColor3f(1f, 0f, 0f);

            glVertex2f(0.75f, 0.75f);
            glColor3f(0f, 1f, 0f);

            glVertex2f(-0.75f, 0.75f);
            glColor3f(0f, 0f, 1f);

            glEnd();
        });

        glfwFramebuffer2.setupRenderLoop(16, TimeUnit.MILLISECOND, () -> {
            glClearColor(0f, 1f, 0f, 1f);
            glClear(GL_COLOR_BUFFER_BIT);

            glBegin(GL_TRIANGLES);

            glVertex2f(0, -0.75f);
            glColor3f(1f, 0f, 0f);

            glVertex2f(0.75f, 0.75f);
            glColor3f(0f, 1f, 0f);

            glVertex2f(-0.75f, 0.75f);
            glColor3f(0f, 0f, 1f);

            glEnd();
        });
    }

    private static float time = 0f;
    private static long lastTime = System.currentTimeMillis();

    public static void tick() {
        Graphics2D renderer = framebuffer.getRenderer();
        renderer.setColor(Color.BLACK);
        renderer.clearRect(0, 0, 128, 128);
        renderer.setColor(Color.WHITE);
        renderer.drawString("Hello from", 0, 10);
        renderer.drawString("Graphics2D!", 0, 20);

        long currentTime = System.currentTimeMillis();
        long l = currentTime / 60;
        if(l % 2 == 0) {
            renderer.setColor(Color.RED);
        }
        renderer.fillRect(128-10, 0, 10, 10);

        renderer.setColor(Color.GREEN);
        float dt = (currentTime-lastTime)/1000.0f;
        lastTime = currentTime;
        time += dt;
        float speed = 10f;
        int x = (int) (Math.cos(time*speed) * 10 + 64) - 25;
        int y = (int) (Math.sin(time*speed) * 10 + 64) - 10;
        renderer.fillRoundRect(x, y, 50, 20, 10, 10);

        renderer.setColor(Color.ORANGE);
        renderer.drawString("Hi :-)", x+16, y+15);

        MapDataPacket mapDataPacket = new MapDataPacket();
        mapDataPacket.mapId = MAP_ID;
        framebuffer.preparePacket(mapDataPacket);
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(p -> {
            p.getPlayerConnection().sendPacket(mapDataPacket);
        });

        mapDataPacket.mapId = OPENGL_MAP_ID;
        glfwFramebuffer.preparePacket(mapDataPacket);
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(p -> {
            p.getPlayerConnection().sendPacket(mapDataPacket);
        });

        mapDataPacket.mapId = OPENGL2_MAP_ID;
        glfwFramebuffer2.preparePacket(mapDataPacket);
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(p -> {
            p.getPlayerConnection().sendPacket(mapDataPacket);
        });
    }
}
