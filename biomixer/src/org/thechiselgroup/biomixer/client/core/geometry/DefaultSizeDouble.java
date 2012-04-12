package org.thechiselgroup.biomixer.client.core.geometry;

public class DefaultSizeDouble implements SizeDouble {

    private final double width;

    private final double height;

    public DefaultSizeDouble(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public double getWidth() {
        return width;
    }

}