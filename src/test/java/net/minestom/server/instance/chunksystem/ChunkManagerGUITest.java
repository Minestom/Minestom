package net.minestom.server.instance.chunksystem;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.event.instance.InstanceChunkLoadEvent;
import net.minestom.server.event.instance.InstanceChunkUnloadEvent;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@EnvTest
public class ChunkManagerGUITest {
    private static final int IMG_WIDTH = 200, IMG_HEIGHT = 200;
    private static final int RGB_WHITE = 0xFFFFFF;

    Env env;
    Instance instance;
    JFrame frame;
    BufferedImage image;
    ImageIcon imageIcon;
    JLabel imageLabel;
    volatile boolean submitted = false;
    volatile boolean submittedAgain = false;

//    @Disabled
    @Test
    void gui(Env env) {
        this.env = env;
        instance = env.createFlatInstance();
        instance.setGenerator(unit -> {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
        });
        try {
//            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }


        // TODO not quite implemented yet
        var onClose = new CompletableFuture<Void>();
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1000, 1000);
        frame.setLocation(400, 250);

        image = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_RGB);

        for (var y = 0; y < image.getHeight(); y++) {
            for (var x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, RGB_WHITE);
            }
        }
        imageIcon = new ImageIcon(image.getScaledInstance(800, 800, BufferedImage.SCALE_FAST));
        imageLabel = new JLabel(imageIcon);
        frame.add(imageLabel);

        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onClose.complete(null);
            }
        });

        addMouse();
        registerEvents();

        env.tickWhile(() -> !onClose.isDone(), Duration.ofDays(365));
    }

    ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

    void work() {
        while (true) {
            var task = queue.poll();
            if (task == null) {
                imageIcon.setImage(image.getScaledInstance(800, 800, BufferedImage.SCALE_FAST));
                imageLabel.repaint();
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

    void registerEvents() {
        var s = (Runnable) () -> {
            if (submitted) {
                submittedAgain = true;
                return;
            }
            submitted = true;
            SwingUtilities.invokeLater(this::work);
        };
        var events = env.process().eventHandler();
        events.addListener(InstanceChunkLoadEvent.class, event -> {
            var x = event.getChunkX();
            var z = event.getChunkZ();
            if (x < 0 || z < 0 || x >= IMG_WIDTH || z >= IMG_HEIGHT) return;
            queue.offer(() -> {
                image.setRGB(x, z, Color.RED.getRGB());
            });
            s.run();
        });
        events.addListener(InstanceChunkUnloadEvent.class, event -> {
            var x = event.getChunkX();
            var z = event.getChunkZ();
            if (x < 0 || z < 0 || x >= IMG_WIDTH || z >= IMG_HEIGHT) return;
            queue.offer(() -> {
                image.setRGB(x, z, RGB_WHITE);
            });
            s.run();
        });
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

                int x = e.getX() - (imageLabel.getWidth() - image.getWidth() * 4) / 2;
                int y = e.getY() - (imageLabel.getHeight() - image.getHeight() * 4) / 2;
                move(x, y);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed.remove(e.getButton());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getX() - (imageLabel.getWidth() - image.getWidth() * 4) / 2;
                int y = e.getY() - (imageLabel.getHeight() - image.getHeight() * 4) / 2;
                move(x, y);
            }

            private void move(int x, int z) {
                x -= 2;
                z -= 2;
                x /= 4;
                z /= 4;
                if (pressed.contains(MouseEvent.BUTTON1)) {
                    var claim = instance.getChunkManager().addClaim(x, z, 20, ChunkClaim.Shape.CIRCLE);
                    unload();
                    this.claim = claim.chunkClaim();
                }
            }

            private void unload() {
                if (claim == null) return;
                instance.getChunkManager().removeClaim(claim);
            }
        };
        imageLabel.addMouseListener(l);
        imageLabel.addMouseMotionListener(l);
    }
}
