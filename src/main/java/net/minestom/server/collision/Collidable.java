package net.minestom.server.collision;

public interface Collidable {
    double minX();
    double minY();
    double minZ();
    double maxX();
    double maxY();
    double maxZ();

    double height();
    double width();
    double depth();

    Collidable ZERO = new Collidable() {
        @Override
        public double minX() {
            return 0;
        }

        @Override
        public double minY() {
            return 0;
        }

        @Override
        public double minZ() {
            return 0;
        }

        @Override
        public double maxX() {
            return 0;
        }

        @Override
        public double maxY() {
            return 0;
        }

        @Override
        public double maxZ() {
            return 0;
        }

        @Override
        public double height() {
            return 0;
        }

        @Override
        public double width() {
            return 0;
        }

        @Override
        public double depth() {
            return 0;
        }
    };
}
