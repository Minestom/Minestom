package net.minestom.server.instance.chunksystem;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

@EnvTest
public class ChunkManagerGUITest {
    private static final int IMG_WIDTH = 400, IMG_HEIGHT = 400;
    private static final int DISPLAY_WIDTH = 800, DISPLAY_HEIGHT = 800;
    private static final int RGB_WHITE = 0xFFFFFFFF;

    //    static {
//        System.setProperty("minestom.chunk-system-priority-drop", "square");
//    }
    Env env;
    Instance instance;
    ChunkManager manager;
    List<Overlay> overlays = new ArrayList<>();
    Overlay mainOverlay;
    Overlay saveOverlay;
    Overlay updateOverlay;
    JFrame frame;
    int loadedChunks = 0;
    int savingChunks = 0;
    JLabel loadedChunksLabel;
    JLabel savingChunksLabel;
    UpdateType[] updateTypes = UpdateType.values();
    JLabel[] sizeLabels = new JLabel[updateTypes.length];
    int[] updateQueueSizes = new int[updateTypes.length];
    volatile boolean submitted = false;
    volatile boolean submittedAgain = false;

    class Overlay {
        String name;
        BufferedImage image;
        ImageIcon icon;
        JLabel label;
        Button button;
        boolean changed = true;

        public Overlay(String name) {
            this.name = name;
            image = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            icon = new ImageIcon();
            label = new JLabel(icon);
            button = new Button("Toggle" + name);
            button.addActionListener(e -> setVisible(!label.isVisible()));
            setVisible(true);
        }

        void setVisible(boolean visible) {
            label.setVisible(visible);
            button.setBackground(visible ? Color.GREEN : Color.RED);
        }

        void setColor(int x, int y, int color) {
            image.setRGB(x, y, color);
            changed = true;
        }

        void updateIcon() {
            if (!changed) return;
            changed = false;
            icon.setImage(image.getScaledInstance(DISPLAY_WIDTH, DISPLAY_HEIGHT, BufferedImage.SCALE_FAST));
            label.repaint();
        }

        void fill(int color) {
            for (var y = 0; y < image.getHeight(); y++) {
                for (var x = 0; x < image.getWidth(); x++) {
                    image.setRGB(x, y, color);
                }
            }
        }
    }

    // works but disabled cause this is again just visualization and doesn't test anything
    @Disabled
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
    @Disabled
    @Test
    void gui(Env env) {
        ServerFlag.ASYNC_CHUNK_SYSTEM = true;
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
        instance.getChunkManager().setChunkLoader(new ChunkLoader() {
            @Override
            public @Nullable Chunk loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
                return null;
            }

            @Override
            public void saveChunk(@NotNull Chunk chunk) {
                if (ThreadLocalRandom.current().nextDouble() < 0.5)
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
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

        saveOverlay = new Overlay("saves");
        overlays.add(saveOverlay);
        updateOverlay = new Overlay("updates");
        overlays.add(updateOverlay);
        mainOverlay = new Overlay("main");
        mainOverlay.fill(RGB_WHITE);
        overlays.add(mainOverlay);

        var imagePanel = new JPanel();
        imagePanel.setLayout(new OverlayLayout(imagePanel));
        for (var overlay : overlays) {
            overlay.updateIcon();
            imagePanel.add(overlay.label);
        }

        var scrollpane = new JScrollPane(imagePanel);
        scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        var panel = new JPanel(new BorderLayout());
        var desc = new JPanel(new GridLayout(5, 1));

        for (var i = 0; i < UpdateType.values().length; i++) {
            sizeLabels[i] = new JLabel();
            updateSize(i);
            desc.add(sizeLabels[i]);
        }
        loadedChunksLabel = new JLabel();
        savingChunksLabel = new JLabel();
        updateLoadedChunks();
        updateSavingChunks();
        desc.add(loadedChunksLabel);
        desc.add(savingChunksLabel);

        for (var overlay : overlays) {
            desc.add(overlay.button);
        }

        panel.add(desc, BorderLayout.EAST);
        panel.add(scrollpane, BorderLayout.WEST);

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
//        start2();

//        Thread.ofPlatform().daemon(true).start(() -> {
//            for (var i = 0; i < 100; i++) {
//                manager.addClaim(150, i, 0, 10, ChunkClaim.Shape.SQUARE);
//            }
//            manager.addClaim(140, 50, 30, 20, ChunkClaim.Shape.SQUARE);
//            System.out.println(manager.getLoadedChunks().size());
//        });


        if (!ServerFlag.ASYNC_CHUNK_SYSTEM)
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
//            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
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

    private void updateSavingChunks() {
        savingChunksLabel.setText("Saving chunks: " + savingChunks);
    }

    ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

    void work() {
        while (true) {
            var task = queue.poll();
            if (task == null) {
                for (var overlay : overlays) {
                    overlay.updateIcon();
                }
                updateLoadedChunks();
                updateSavingChunks();
                for (var i = 0; i < updateTypes.length; i++) {
                    updateSize(i);
                }
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
        offer(() -> mainOverlay.setColor(x, z, color));
    }

    void colorizeUpdate(int x, int z, int color) {
        if (x < 0 || z < 0 || x >= IMG_WIDTH || z >= IMG_HEIGHT) return;
        updateOverlay.setColor(x, z, color);
    }

    Long2ObjectMap<Chunk> saving = new Long2ObjectOpenHashMap<>();

    void colorizeSave(Chunk chunk, boolean start, int color) {
        var x = chunk.getChunkX();
        var z = chunk.getChunkZ();
        if (x < 0 || z < 0 || x >= IMG_WIDTH || z >= IMG_HEIGHT) return;
        offer(() -> {
            if (start) {
                savingChunks++;
                saving.put(CoordConversion.chunkIndex(x, z), chunk);
            } else {
                savingChunks--;
                if (!saving.remove(CoordConversion.chunkIndex(x, z), chunk)) return;
            }
            saveOverlay.setColor(x, z, color);
        });
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

            @Override
            public void onSaveStarted(Chunk chunk) {
                colorizeSave(chunk, true, 0x4F00FF00);
            }

            @Override
            public void onSaveComplete(Chunk chunk) {
                colorizeSave(chunk, false, 0);
            }
        };
    }

    void addMouse() {
        var keyPressed = new IntOpenHashSet();
        var factX = DISPLAY_WIDTH / IMG_WIDTH;
        var factZ = DISPLAY_HEIGHT / IMG_HEIGHT;
        var mainImage = mainOverlay.image;
        var mainImageLabel = mainOverlay.label;
        var l = new MouseInputAdapter() {
            private final ReentrantLock lock = new ReentrantLock();
            private final IntSet pressed = new IntOpenHashSet();
            private final WeakHashMap<ChunkClaim, Void> finishedClaims = new WeakHashMap<>();
            private final Int2ObjectMap<LongSet> loadedChunks = new Int2ObjectOpenHashMap<>();
            private final Int2ObjectMap<ChunkClaim> claims = new Int2ObjectOpenHashMap<>();

            private ClaimCallbacks callbacks(int button) {
                return new ClaimCallbacks() {

                    @Override
                    public void allChunksLoaded(@NotNull ChunkClaim claim) {
                        lock.lock();
                        try {
                            finishedClaims.put(claim, null);
                        } finally {
                            lock.unlock();
                        }
                        System.out.println("All loaded");
                    }

                    @Override
                    public void chunkLoaded(@NotNull ChunkClaim claim, @NotNull Chunk chunk) {
                        boolean changed;
                        int size;
                        lock.lock();
                        try {
                            if (finishedClaims.containsKey(claim)) {
                                System.err.println("Load callback after #allChunksLoaded. This is incorrect behavior");
                                Thread.dumpStack();
                            }
                            var s = loadedChunks.get(button);
                            if (s != null) {
                                changed = s.add(CoordConversion.chunkIndex(chunk.getChunkX(), chunk.getChunkZ()));
                                size = s.size();
                            } else {
                                changed = false;
                                size = 0;
                            }
                        } finally {
                            lock.unlock();
                        }
                        if (changed)
                            System.out.println("Chunk loaded: " + chunk.getChunkX() + " " + chunk.getChunkZ() + " --- " + size);
                    }
                };
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed.add(e.getButton());

                if (keyPressed.contains(KeyEvent.VK_SHIFT)) {
                    unload(e.getButton(), null);
                    return;
                }

                int x = e.getX();
                int y = e.getY();
                move(x, y, e.getButton());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed.remove(e.getButton());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getX() - (mainImageLabel.getWidth() - mainImage.getWidth() * factX) / 2;
                int y = e.getY() - (mainImageLabel.getHeight() - mainImage.getHeight() * factZ) / 2;
                if (keyPressed.contains(KeyEvent.VK_SHIFT)) return;
                for (var i : pressed) {
                    move(x, y, i);
                }
            }

            private void move(int x, int z, int btn) {
                x -= factX / 2;
                z -= factZ / 2;
                x /= factX;
                z /= factZ;
                if (pressed.contains(btn)) {
                    this.loadedChunks.putIfAbsent(btn, new LongOpenHashSet());
                    var claim = instance.getChunkManager().addClaim(x, z, 32, 0, ChunkClaim.Shape.CIRCLE, callbacks(btn));
                    unload(btn, claim.claim());
                    this.claims.put(btn, claim.claim());
                }
            }

            private void unload(int btn, @Nullable ChunkClaim newClaim) {
                lock.lock();
                try {
                    var claim = claims.remove(btn);
                    if (claim == null) return;

                    if (newClaim == null) {
                        loadedChunks.remove(btn);
                    } else {
                        var it = loadedChunks.get(btn).longIterator();
                        while (it.hasNext()) {
                            var loadedChunk = it.nextLong();
                            var x = CoordConversion.chunkIndexGetX(loadedChunk);
                            var z = CoordConversion.chunkIndexGetZ(loadedChunk);
                            if (newClaim.contains(x, z)) continue;
                            it.remove();
                        }
                    }
                    instance.getChunkManager().removeClaim(claim);
                } finally {
                    lock.unlock();
                }
            }
        };
        var l2 = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                keyPressed.add(e.getKeyCode());
                System.out.println("Press " + e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keyPressed.remove(e.getKeyCode());
            }
        };
        frame.addKeyListener(l2);
        mainImageLabel.addMouseListener(l);
        mainImageLabel.addMouseMotionListener(l);
    }
}
