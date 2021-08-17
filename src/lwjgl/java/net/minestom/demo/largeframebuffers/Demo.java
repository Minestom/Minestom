package net.minestom.demo.largeframebuffers;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ItemFrameMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.MapMeta;
import net.minestom.server.map.Framebuffer;
import net.minestom.server.map.LargeFramebuffer;
import net.minestom.server.map.MapColors;
import net.minestom.server.map.framebuffers.LargeDirectFramebuffer;
import net.minestom.server.map.framebuffers.LargeGLFWFramebuffer;
import net.minestom.server.map.framebuffers.LargeGraphics2DFramebuffer;
import net.minestom.server.map.framebuffers.MapColorRenderer;
import net.minestom.server.network.packet.server.play.MapDataPacket;
import net.minestom.server.utils.time.TimeUnit;

import java.awt.*;
import java.util.Arrays;
import java.util.function.Consumer;

public class Demo {

    public static void main(String[] args) {
        MainDemo.main(args); // used to avoid code duplication
        initDemo();
    }

    private static void initDemo() {
        InstanceManager instances = MinecraftServer.getInstanceManager();
        Instance instance = instances.getInstances().stream().findAny().get();

        LargeDirectFramebuffer directFramebuffer = new LargeDirectFramebuffer(512, 512);
        LargeGraphics2DFramebuffer graphics2DFramebuffer = new LargeGraphics2DFramebuffer(512, 512);
        LargeGLFWFramebuffer glfwFramebuffer = new LargeGLFWFramebuffer(512, 512);

        glfwFramebuffer.changeRenderingThreadToCurrent();
        OpenGLRendering.init();
        MapColorRenderer renderer = new MapColorRenderer(glfwFramebuffer, OpenGLRendering::render);
        glfwFramebuffer.unbindContextFromThread();

        //   renderingLoop(0, directFramebuffer, Demo::directRendering);
        //   renderingLoop(101, graphics2DFramebuffer, Demo::graphics2DRendering);
        renderingLoop(201, glfwFramebuffer, f -> {
        });

        glfwFramebuffer.setupRenderLoop(15, TimeUnit.MILLISECOND, renderer);

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                instance.loadChunk(x, z);
            }
        }
        setupMaps(instance, 0, 10);
        setupMaps(instance, 101, 20);
        setupMaps(instance, 201, 30);
    }

    private static void createFrame(Instance instance, int id, int x, int y, int z) {
        Entity itemFrame = new Entity(EntityType.ITEM_FRAME);

        ItemFrameMeta itemFrameMeta = (ItemFrameMeta) itemFrame.getEntityMeta();

        itemFrameMeta.setNotifyAboutChanges(false);

        itemFrameMeta.setOrientation(ItemFrameMeta.Orientation.NORTH);

        ItemStack map = ItemStack.builder(Material.FILLED_MAP)
                .meta(new MapMeta.Builder().mapId(id).build())
                .build();

        itemFrameMeta.setItem(map);
        itemFrameMeta.setCustomNameVisible(true);
        itemFrameMeta.setCustomName(Component.text("MapID: " + id));

        itemFrameMeta.setNotifyAboutChanges(true);

        itemFrame.setInstance(instance, new Pos(x, y, z, 180, 0));
    }

    private static void setupMaps(Instance instance, int mapIDStart, int zCoordinate) {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                createFrame(instance, mapIDStart + y * 4 + x, 2 - x, 45 - y, zCoordinate);
            }
        }
    }

    private static <T extends LargeFramebuffer> void renderingLoop(int mapIDStart, T framebuffer, Consumer<T> renderingCode) {
        final Framebuffer[] subviews = new Framebuffer[4 * 4];
        for (int i = 0; i < subviews.length; i++) {
            int x = (i % 4) * 128;
            int y = (i / 4) * 128;
            subviews[i] = framebuffer.createSubView(x, y);
        }
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            renderingCode.accept(framebuffer);
            for (int i = 0; i < subviews.length; i++) {
                final MapDataPacket packet = new MapDataPacket();
                packet.mapId = mapIDStart + i;

                Framebuffer f = subviews[i];
                f.preparePacket(packet);
                sendPacket(packet);
            }
        }).repeat(15, TimeUnit.MILLISECOND).schedule();
    }

    private static void sendPacket(MapDataPacket packet) {
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(p -> p.getPlayerConnection().sendPacket(packet));
    }

    private static void directRendering(LargeDirectFramebuffer framebuffer) {
        Arrays.fill(framebuffer.getColors(), 0, 512 * 40 + 128, MapColors.COLOR_CYAN.baseColor());
        Arrays.fill(framebuffer.getColors(), 512 * 40 + 128, framebuffer.getColors().length, MapColors.COLOR_RED.baseColor());
    }

    private static void graphics2DRendering(LargeGraphics2DFramebuffer framebuffer) {
        Graphics2D renderer = framebuffer.getRenderer();
        renderer.setColor(Color.BLACK);
        renderer.clearRect(0, 0, 512, 512);
        renderer.setColor(Color.WHITE);
        renderer.drawString("Here's a very very long string that needs multiple maps to fit", 0, 100);
    }

}
