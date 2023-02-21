package dev.emortal;

import net.minestom.server.coordinate.Point;

import java.awt.*;

/**
 * Area util class for finding if a position lies within a rectangle.
 * Has no Y values
 */
public class Area {

    private final Point topLeft;
    private final Point bottomRight;

    private final int firstMinX;
    private final int firstMinZ;
    private final int firstMaxX;
    private final int firstMaxZ;

    public Area(Point topLeft, Point bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;

        this.firstMinX = Math.min(topLeft.blockX(), bottomRight.blockX());
        this.firstMinZ = Math.min(topLeft.blockZ(), bottomRight.blockZ());
        this.firstMaxX = Math.max(topLeft.blockX(), bottomRight.blockX());
        this.firstMaxZ = Math.max(topLeft.blockZ(), bottomRight.blockZ());
    }

    public boolean isInside(Point position, int tolerance) {
        var sizeX = Math.abs(firstMaxX - firstMinX);
        var sizeZ = Math.abs(firstMaxZ - firstMinZ);

        var rect = new Rectangle(firstMinX, firstMinZ, sizeX, sizeZ);
        rect.grow(tolerance, tolerance);

        return rect.contains(position.blockX(), position.blockZ());
    }

    @Override
    public String toString() {
        return "Area{" +
                "topLeft=" + topLeft +
                ", bottomRight=" + bottomRight +
                '}';
    }
}
