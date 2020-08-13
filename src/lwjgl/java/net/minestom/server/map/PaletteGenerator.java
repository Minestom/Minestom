package net.minestom.server.map;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PaletteGenerator {

    public static void main(String[] args) {
        Map<Integer, Integer> colors = new HashMap<>();
        int highestIndex = 0;
        for(MapColors c : MapColors.values()) {
            if (c == MapColors.NONE)
                continue;
            for(MapColors.Multiplier m : MapColors.Multiplier.values()) {
                int index = ((int)m.apply(c)) & 0xFF;
                if(index > highestIndex) {
                    highestIndex = index;
                }
                int rgb = MapColors.PreciseMapColor.toRGB(c, m);
                colors.put(index, rgb);
            }
        }

        BufferedImage paletteTexture = new BufferedImage(highestIndex+1, 1, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i <= highestIndex; i++) {
            int rgb = colors.getOrDefault(i, 0);
            int argb = (0xFF << 24) | (rgb & 0xFFFFFF);
            paletteTexture.setRGB(i, 0, argb);
        }

        try {
            ImageIO.write(paletteTexture, "png", new File("src/lwjgl/resources/textures/palette.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
