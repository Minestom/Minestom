package net.minestom.server.instance;

import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.Direction;

import java.util.HashMap;
import java.util.Map;

public class SectionLinkManager {
    private record SectionLink(Section section, Map<BlockFace, Section> faces, int chunkX, int chunkZ, int sectionY) {}
    private static final Map<Integer, Map<Integer, Map<Integer, SectionLink>>> sectionLinks = new HashMap<>();

    public static void addSection(Section section, int chunkX, int chunkZ, int sectionY) {
        Map<Integer, Map<Integer, SectionLink>> foundX = sectionLinks.get(chunkX);
        if (foundX == null) {
            foundX = new HashMap<>();
            sectionLinks.put(chunkX, foundX);
        }

        Map<Integer, SectionLink> foundZ = foundX.get(chunkZ);
        if (foundZ == null) {
            foundZ = new HashMap<>();
            foundX.put(chunkZ, foundZ);
        }

        Map<BlockFace, Section> links = getLinks(chunkX, chunkZ, sectionY);

        var newLink = new SectionLink(section, links, chunkX, chunkZ, sectionY);

        for (var entry : links.entrySet()) {
            BlockFace face = entry.getKey();
            Section link = entry.getValue();
            Direction direction = face.getOppositeFace().toDirection();

            propagateLink(
                    chunkX + direction.normalX(),
                    chunkZ + direction.normalZ(),
                    sectionY + direction.normalY(),
                    face.getOppositeFace(),
                    link);
        }
        foundZ.put(sectionY, newLink);
    }

    // Look up section from given coordinates and link it to the provided section based on the block face
    private static void propagateLink(int chunkX, int chunkZ, int sectionY, BlockFace face, Section section) {
        SectionLink foundLink = getSectionLink(chunkX, chunkZ, sectionY);
        if (foundLink != null) {
            foundLink.faces.put(face, section);
        }
    }

    private static Map<BlockFace, Section> getLinks(int chunkX, int chunkZ, int sectionY) {
        Map<BlockFace, Section> links = new HashMap<>();

        for (BlockFace face : BlockFace.values()) {
            Direction direction = face.toDirection();
            int x = chunkX + direction.normalX();
            int z = chunkZ + direction.normalZ();
            int y = sectionY + direction.normalY();

            SectionLink found = getSectionLink(x, z, y);
            if (found != null) links.put(face, found.section);
        }

        return links;
    }

    private static SectionLink getSectionLink(int chunkX, int chunkZ, int sectionY) {
        Map<Integer, Map<Integer, SectionLink>> foundX = sectionLinks.get(chunkX);
        if (foundX == null) return null;

        Map<Integer, SectionLink> foundZ = foundX.get(chunkZ);
        if (foundZ == null) return null;

        return foundZ.get(sectionY);
    }
}
