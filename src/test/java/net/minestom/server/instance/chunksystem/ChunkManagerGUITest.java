package net.minestom.server.instance.chunksystem;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

public class ChunkManagerGUITest {
    private static final int IMG_WIDTH = 200, IMG_HEIGHT = 200;
    private static final int RGB_WHITE = 0xFFFFFF;

    @Test
    @Disabled // comment this if you want to test the UI to visualize chunk loading
    void gui() {
        // TODO not quite implemented yet
        var onClose = new CompletableFuture<Void>();
        var frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setLocation(400, 250);

        var image = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (var y = 0; y < image.getHeight(); y++) {
            for (var x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, RGB_WHITE);
            }
        }
        var imageIcon = new ImageIcon(image.getScaledInstance(800, 800, BufferedImage.SCALE_FAST));
        frame.add(new JLabel(imageIcon));

        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onClose.complete(null);
            }
        });
        onClose.join();
    }
}
