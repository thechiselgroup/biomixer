/*******************************************************************************
 * Copyright 2012 David Rusk 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed;

/**
 * A two-dimensional vector for modelling forces, velocities, displacements,
 * etc.
 * 
 * @author drusk
 * 
 */
public class Vector2D {

    private double xComponent;

    private double yComponent;

    public Vector2D(double xComponent, double yComponent) {
        this.xComponent = xComponent;
        this.yComponent = yComponent;
    }

    /**
     * Adds another vector to this vector. Returns a reference to this vector
     * for convenience.
     * 
     * @param other
     *            the vector being added with this one
     * @return the vector resulting from the addition.
     */
    public Vector2D add(Vector2D other) {
        xComponent += other.getXComponent();
        yComponent += other.getYComponent();
        return this;
    }

    /**
     * @return this vector's direction in radians with respect to the positive
     *         side of the x-axis.
     */
    public double getDirection() {
        return Math.atan2(yComponent, xComponent);
    }

    /**
     * @return the magnitude (scalar) of this vector.
     */
    public double getMagnitude() {
        return Math.sqrt(xComponent * xComponent + yComponent * yComponent);
    }

    /**
     * @return the vector's projection along the x-axis.
     */
    public double getXComponent() {
        return xComponent;
    }

    /**
     * @return the vector's projection along the y-axis.
     */
    public double getYComponent() {
        return yComponent;
    }

    /**
     * Scales this vector by the given value. Returns a reference to this vector
     * for convenience.
     * 
     * @param scalar
     *            value to scale by
     * @return the scaled vector
     */
    public Vector2D scaleBy(double scalar) {
        xComponent *= scalar;
        yComponent *= scalar;
        return this;
    }

    @Override
    public String toString() {
        return "[" + xComponent + ", " + yComponent + "]";
    }

}
