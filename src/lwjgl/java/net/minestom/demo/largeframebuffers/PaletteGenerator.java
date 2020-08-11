package net.minestom.demo.largeframebuffers;

import net.minestom.server.map.MapColors;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PaletteGenerator {

    public static void main(String[] args) {
        Map<Byte, Integer> colors = new HashMap<>();
        int highestIndex = 0;
        for(MapColors c : MapColors.values()) {
            for(MapColors.Multiplier m : MapColors.Multiplier.values()) {
                byte index = m.apply(c);
                if(((int)index & 0xFF) > highestIndex) {
                    highestIndex = ((int)index) & 0xFF;
                }
                int rgb = MapColors.PreciseMapColor.toRGB(c, m);
                colors.put(index, rgb);
            }
        }

        BufferedImage paletteTexture = new BufferedImage(highestIndex+1, 1, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i <= highestIndex; i++) {
            int rgb = colors.getOrDefault((byte)i, 0);
            int argb = (0xFF << 24) | rgb;
            paletteTexture.setRGB(i, 0, argb);
        }

        try {
            ImageIO.write(paletteTexture, "png", new File("src/lwjgl/resources/textures/palette.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
