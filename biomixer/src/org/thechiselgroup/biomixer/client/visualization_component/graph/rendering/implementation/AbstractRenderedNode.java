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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation;

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNode;

public abstract class AbstractRenderedNode implements RenderedNode {

    @Override
    public PointDouble getCentre() {
        return new PointDouble(getLeftX() + getSize().getWidth() / 2, getTopY()
                + getSize().getHeight() / 2);
    }

    @Override
    public PointDouble getTopLeft() {
        return new PointDouble(getLeftX(), getTopY());
    }

}
