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

public class Vector2DFactory {

    public static Vector2D createVectorFromCartesianCoordinates(
            double xComponent, double yComponent) {
        return new Vector2D(xComponent, yComponent);
    }

    public static Vector2D createVectorFromPolarCoordinates(
            double magnitude, double direction) {
        return new Vector2D(magnitude * Math.cos(direction), magnitude
                * Math.sin(direction));
    }

}
