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

public class DefaultVector2D implements Vector2D {

    private double xComponent;

    private double yComponent;

    public DefaultVector2D(double xComponent, double yComponent) {
        this.xComponent = xComponent;
        this.yComponent = yComponent;
    }

    @Override
    public Vector2D add(Vector2D other) {
        xComponent += other.getXComponent();
        yComponent += other.getYComponent();
        return this;
    }

    @Override
    public double getDirection() {
        return Math.atan2(yComponent, xComponent);
    }

    @Override
    public double getMagnitude() {
        return Math.sqrt(xComponent * xComponent + yComponent * yComponent);
    }

    @Override
    public double getXComponent() {
        return xComponent;
    }

    @Override
    public double getYComponent() {
        return yComponent;
    }

    @Override
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
