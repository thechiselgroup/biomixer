package org.thechiselgroup.biomixer.client.core.geometry;

/**
 * Convenience class for when the size will be the same for height and width.
 * 
 * @author everbeek
 * 
 */
public class SquareSizeDouble implements SizeDouble {

    double size;

    public SquareSizeDouble(double size) {
        this.size = size;
    }

    @Override
    public double getHeight() {
        return this.size;
    }

    @Override
    public double getWidth() {
        return this.size;
    }

}
