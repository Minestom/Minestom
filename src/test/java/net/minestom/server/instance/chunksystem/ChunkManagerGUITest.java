package net.minestom.server.instance.chunksystem;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.ServerFlag;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@EnvTest
public class ChunkManagerGUITest {
    private static final int IMG_WIDTH = 200, IMG_HEIGHT = 200;
    private static final int RGB_WHITE = 0xFFFFFF;

    //    static {
//        System.setProperty("minestom.chunk-system-priority-drop", "square");
//    }
    Env env;
    Instance instance;
    ChunkManager manager;
    JFrame frame;
    BufferedImage mainImage;
    ImageIcon mainImageIcon;
    BufferedImage updateImage;
    ImageIcon updateImageIcon;
    JLabel mainImageLabel;
    JLabel updateImageLabel;
    int loadedChunks = 0;
    JLabel loadedChunksLabel;
    UpdateType[] updateTypes = UpdateType.values();
    JLabel[] sizeLabels = new JLabel[updateTypes.length];
    int[] updateQueueSizes = new int[updateTypes.length];
    volatile boolean submitted = false;
    volatile boolean submittedAgain = false;

    @Test
    void visualizeDrop() {
        var drop = new PriorityDrop.Square();
        var offsetX = 90;
        var offsetZ = 120;
        var a = new double[61][61];
        for (var z = 0; z < 31; z++) {
            for (var x = 0; x < 31; x++) {
                a[30 + z][30 + x] = drop.calculate(offsetX, offsetZ, offsetX + x, offsetZ + z);
                if (x != 0) {
                    a[30 + z][30 - x] = drop.calculate(offsetX, offsetZ, offsetX - x, offsetZ + z);
                    if (z != 0) {
                        a[30 - z][30 - x] = drop.calculate(offsetX, offsetZ, offsetX - x, offsetZ - z);
                    }
                }
                if (z != 0) {
                    a[30 - z][30 + x] = drop.calculate(offsetX, offsetZ, offsetX + x, offsetZ - z);
                }
            }
        }
        for (var doubles : a) {
            for (var i = 0; i < doubles.length; i++) {
                doubles[i] += 10;
            }
        }
        for (var r : a) {
            System.out.println(Arrays.toString(r));
        }
    }

    // Enable this test for manual GUI visualization
//    @Disabled
    @Test
    void gui(Env env) {
//        ServerFlag.ASYNC_CHUNK_SYSTEM = true;
        var s = new CompletableFuture<Void>();
        Thread.ofPlatform().daemon(true).start(() -> {
            s.complete(null);
            LockSupport.parkNanos(9223372036854775783L);
        });
        s.join();
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
        this.env = env;
        instance = env.createFlatInstance();
        manager = instance.getChunkManager();
        instance.getChunkManager().setAutosaveEnabled(true);
        instance.getChunkManager().setChunkLoader(new IChunkLoader() {
            @Override
            public @Nullable Chunk loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
                return null;
            }

            @Override
            public void saveChunk(@NotNull Chunk chunk) {
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(200));
            }
        });
        instance.setGenerator(unit -> {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        });


        // TODO not quite implemented yet
        var onClose = new CompletableFuture<Void>();
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1300, 1000);
        frame.setLocation(400, 250);

        mainImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_RGB);

        for (var y = 0; y < mainImage.getHeight(); y++) {
            for (var x = 0; x < mainImage.getWidth(); x++) {
                mainImage.setRGB(x, y, RGB_WHITE);
            }
        }
        updateImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        mainImageIcon = new ImageIcon(mainImage.getScaledInstance(800, 800, BufferedImage.SCALE_FAST));
        updateImageIcon = new ImageIcon(updateImage.getScaledInstance(800, 800, BufferedImage.SCALE_FAST));
        mainImageLabel = new JLabel(mainImageIcon);
        updateImageLabel = new JLabel(updateImageIcon);

        var imagePanel = new JPanel();
        imagePanel.setLayout(new OverlayLayout(imagePanel));
        imagePanel.add(updateImageLabel);
        imagePanel.add(mainImageLabel);

        var panel = new JPanel(new BorderLayout());
        var desc = new JPanel(new GridLayout(5, 1));

        for (var i = 0; i < UpdateType.values().length; i++) {
            sizeLabels[i] = new JLabel();
            updateSize(i);
            desc.add(sizeLabels[i]);
        }
        loadedChunksLabel = new JLabel();
        updateLoadedChunks();
        desc.add(loadedChunksLabel);

        panel.add(desc, BorderLayout.EAST);
        panel.add(imagePanel, BorderLayout.WEST);

        frame.add(panel, BorderLayout.CENTER);

        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onClose.complete(null);
            }
        });

        addMouse();
        registerEvents();
//        start1();

        Thread.ofPlatform().daemon(true).start(() -> {
            for (var i = 0; i < 100; i++) {
                manager.addClaim(150, i, 0, 10, ChunkClaim.Shape.SQUARE);
            }
            manager.addClaim(140, 50, 30, 20, ChunkClaim.Shape.SQUARE);
            System.out.println(manager.getLoadedChunks().size());
        });


        onClose.join();

        env.tickWhile(() -> !onClose.isDone(), Duration.ofDays(365));
        ServerFlag.ASYNC_CHUNK_SYSTEM = false;
    }

    private void start1() {
        Thread.startVirtualThread(() -> {
            var futures = new CompletableFuture[100];
            for (var i = 0; i < 100; i++) {
                futures[i] = manager.addClaim(150, i, 0, 10, ChunkClaim.Shape.SQUARE).chunkFuture();
            }
            CompletableFuture.allOf(futures).join();
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
            manager.addClaim(140, 50, 30, 20, ChunkClaim.Shape.SQUARE).chunkFuture().join();
        });
    }

    private void start2() {
        Thread.startVirtualThread(() -> {
            manager.addClaim(150, 150, 20, 40, ChunkClaim.Shape.DIAMOND);
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
            manager.addClaim(150, 150, 20, 40, ChunkClaim.Shape.SQUARE);
        });
    }

    private void updateSize(int i) {
        sizeLabels[i].setText(updateTypes[i] + " queue size: " + updateQueueSizes[i]);
    }

    private void updateLoadedChunks() {
        loadedChunksLabel.setText("Loaded chunks: " + loadedChunks);
    }

    ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

    void work() {
        while (true) {
            var task = queue.poll();
            if (task == null) {
                mainImageIcon.setImage(mainImage.getScaledInstance(800, 800, BufferedImage.SCALE_FAST));
                updateImageIcon.setImage(updateImage.getScaledInstance(800, 800, BufferedImage.SCALE_FAST));
                updateLoadedChunks();
                for (var i = 0; i < updateTypes.length; i++) {
                    updateSize(i);
                }
                mainImageLabel.repaint();
                updateImageLabel.repaint();
                if (submittedAgain) {
                    submittedAgain = false;
                    SwingUtilities.invokeLater(this::work);
                    return;
                }
                submitted = false;
                return;
            }
            task.run();
        }
    }

    void colorize(int x, int z, int color) {
        if (x < 0 || z < 0 || x >= IMG_WIDTH || z >= IMG_HEIGHT) return;
        offer(() -> mainImage.setRGB(x, z, color));
    }

    void colorizeUpdate(int x, int z, int color) {
        if (x < 0 || z < 0 || x >= IMG_WIDTH || z >= IMG_HEIGHT) return;
        updateImage.setRGB(x, z, color);
    }

    void offer(Runnable runnable) {
        queue.offer(runnable);
        submit();
    }

    private void submit() {
        if (submitted) {
            submittedAgain = true;
            return;
        }
        submitted = true;
        SwingUtilities.invokeLater(this::work);
    }

    void registerEvents() {
        SingleThreadedManager.callbacks = new InternalCallbacks() {
            @Override
            public void onAddClaim(int x, int z, ChunkClaim chunkClaim) {
                InternalCallbacks.super.onAddClaim(x, z, chunkClaim);
            }

            @Override
            public void onRemoveClaim(int x, int z, ChunkClaim chunkClaim) {
                InternalCallbacks.super.onRemoveClaim(x, z, chunkClaim);
            }

            @Override
            public void onLoadStarted(int x, int z) {
                colorize(x, z, Color.YELLOW.getRGB());
            }

            @Override
            public void onLoadCompleted(int x, int z) {
                colorize(x, z, Color.GREEN.getRGB());
                offer(() -> loadedChunks++);
            }

            @Override
            public void onLoadCancelled(int x, int z) {
                colorize(x, z, Color.WHITE.getRGB());
            }

            @Override
            public void onGenerationStarted(int x, int z) {
                colorize(x, z, Color.BLUE.getRGB());
            }

            @Override
            public void onGenerationCompleted(int x, int z) {
                colorize(x, z, Color.CYAN.getRGB());
            }

            @Override
            public void onUnloadStarted(int x, int z) {
                colorize(x, z, Color.ORANGE.darker().getRGB());
                offer(() -> loadedChunks--);
            }

            @Override
            public void onUnloadCompleted(int x, int z) {
                colorize(x, z, Color.WHITE.getRGB());
            }

            @Override
            public void addUpdate(int x, int z, UpdateType updateType) {
                offer(() -> {
                    updateQueueSizes[updateType.ordinal()]++;
                    colorizeUpdate(x, z, 0x4FFF00FF);
                });
            }

            @Override
            public void removeUpdate(int x, int z, UpdateType updateType) {
                offer(() -> {
                    updateQueueSizes[updateType.ordinal()]--;
                    colorizeUpdate(x, z, 0);
                });
            }
        };
    }

    void addMouse() {
        var l = new MouseInputAdapter() {
            private final IntSet pressed = new IntOpenHashSet();
            private ChunkClaim claim = null;

            @Override
            public void mousePressed(MouseEvent e) {
                pressed.add(e.getButton());

                if (e.getButton() == MouseEvent.BUTTON2) {
                    unload();
                }

                int x = e.getX() - (mainImageLabel.getWidth() - mainImage.getWidth() * 4) / 2;
                int y = e.getY() - (mainImageLabel.getHeight() - mainImage.getHeight() * 4) / 2;
                move(x, y);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed.remove(e.getButton());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getX() - (mainImageLabel.getWidth() - mainImage.getWidth() * 4) / 2;
                int y = e.getY() - (mainImageLabel.getHeight() - mainImage.getHeight() * 4) / 2;
                move(x, y);
            }

            private void move(int x, int z) {
                x -= 2;
                z -= 2;
                x /= 4;
                z /= 4;
                if (pressed.contains(MouseEvent.BUTTON1)) {
                    var claim = instance.getChunkManager().addClaim(x, z, 35, ChunkClaim.Shape.SQUARE);
                    unload();
                    this.claim = claim.chunkClaim();
                }
            }

            private void unload() {
                if (claim == null) return;
                instance.getChunkManager().removeClaim(claim);
            }
        };
        mainImageLabel.addMouseListener(l);
        mainImageLabel.addMouseMotionListener(l);
    }
}
