package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SectionLinkManager {
    public Point getPosition(Section section) {
        return sectionLookup.get(section);
    }

    private record SectionLink(Section section, Map<BlockFace, Section> faces, int chunkX, int chunkZ, int sectionY) {}
    private final Map<Point, SectionLink> sectionLinks = new ConcurrentHashMap<>();
    private final Map<Section, Point> sectionLookup = new ConcurrentHashMap<>();
    private final Set<Section> requiresLightUpdate = ConcurrentHashMap.newKeySet();

    public void addSection(Section section, int chunkX, int chunkZ, int sectionY) {
        Vec sectionPos = new Vec(chunkX, sectionY, chunkZ);
        sectionLookup.put(section, sectionPos);

        Map<BlockFace, Section> links = getLinks(sectionPos);

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
        sectionLinks.put(new Vec(chunkX, sectionY, chunkZ), newLink);
    }

    // Look up section from given coordinates and link it to the provided section based on the block face
    private void propagateLink(int chunkX, int chunkZ, int sectionY, BlockFace face, Section section) {
        SectionLink foundLink = getSectionLink(chunkX, chunkZ, sectionY);
        if (foundLink != null) {
            foundLink.faces.put(face, section);
        }
    }

    private Map<BlockFace, Section> getLinks(Point section) {
        int chunkX = section.blockX();
        int chunkZ = section.blockZ();
        int sectionY = section.blockY();

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

    private SectionLink getSectionLink(int chunkX, int chunkZ, int sectionY) {
        return sectionLinks.get(new Vec(chunkX, sectionY, chunkZ));
    }

    public @NotNull Map<BlockFace, Section> getNeighbors(Section section) {
        Point sectionPos = sectionLookup.get(section);
        if (sectionPos == null) return new HashMap<>();
        return getLinks(sectionPos);
    }

    public void queueLightUpdate(Section section) {
        requiresLightUpdate.add(section);
    }
}
