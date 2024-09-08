package net.minestom.server.instance.painter;

import java.util.function.Consumer;

record PainterImpl(Consumer<Painter.World> consumer) implements Painter {
}
