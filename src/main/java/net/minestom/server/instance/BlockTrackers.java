package net.minestom.server.instance;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class BlockTrackers extends CopyOnWriteArrayList<BlockTrackers.Entry> {
    record Entry(Vec min, Vec max, Block.Tracker tracker) {

        public Entry(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Block.Tracker tracker) {
            this(new Vec(minX, minY, minZ), new Vec(maxX, maxY, maxZ), tracker);
        }
    }
}
