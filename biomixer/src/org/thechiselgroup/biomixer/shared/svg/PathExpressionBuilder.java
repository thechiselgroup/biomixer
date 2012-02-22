/*******************************************************************************
 * Copyright 2011 Lars Grammel. All rights reserved.
 *******************************************************************************/
package org.thechiselgroup.choosel.svg.client;

/**
 * Utility class for creating path expressions.
 * 
 * @author Lars Grammel
 * 
 * @see http://www.w3.org/TR/2003/REC-SVG11-20030114/paths.html
 */
// XXX currently only implements parts of the path spec
public class PathExpressionBuilder {

    private static final String SEPARATOR = " ";

    private static final String NEWLINE = "\n";

    private StringBuilder pathExpression = new StringBuilder();

    /**
     * Draws an elliptical arc from the current point to (x, y). The size and
     * orientation of the ellipse are defined by two radii (rx, ry) and an
     * x-axis-rotation, which indicates how the ellipse as a whole is rotated
     * relative to the current coordinate system. The center (cx, cy) of the
     * ellipse is calculated automatically to satisfy the constraints imposed by
     * the other parameters. large-arc and sweep contribute to the automatic
     * calculations and help determine how the arc is drawn.
     */
    public PathExpressionBuilder arc(double rx, double ry,
            double xAxisRotation, boolean largeArc, boolean sweepClockwise,
            double x, double y) {

        pathExpression.append("A").append(rx).append(SEPARATOR).append(ry)
                .append(SEPARATOR).append(xAxisRotation).append(SEPARATOR)
                .append(largeArc ? '1' : '0').append(SEPARATOR)
                .append(sweepClockwise ? '1' : '0').append(SEPARATOR).append(x)
                .append(SEPARATOR).append(y);
        return this;
    }

    public PathExpressionBuilder circle(double radius) {
        return moveTo(0, radius).arc(radius, radius, 0, true, true, 0, -radius)
                .arc(radius, radius, 0, true, true, 0, radius);
    }

    public PathExpressionBuilder close() {
        pathExpression.append("Z");
        return this;
    }

    public PathExpressionBuilder lineTo(double x, double y) {
        pathExpression.append("L").append(x).append(SEPARATOR).append(y);
        return this;
    }

    /**
     * Start a new sub-path at the given (x,y) coordinate.
     */
    public PathExpressionBuilder moveTo(double x, double y) {
        pathExpression.append("M").append(x).append(SEPARATOR).append(y);
        return this;
    }

    @Override
    public String toString() {
        return pathExpression.toString();
    }

}