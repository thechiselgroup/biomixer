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

import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;

public abstract class AbstractRenderedArc implements RenderedArc {

    private Arc arc;

    protected RenderedNode source;

    protected RenderedNode target;

    protected AbstractRenderedArc(Arc arc, RenderedNode source,
            RenderedNode target) {
        this.arc = arc;
        this.source = source;
        this.target = target;
    }

    @Override
    public Arc getArc() {
        return arc;
    }

    @Override
    public RenderedNode getSource() {
        return source;
    }

    @Override
    public RenderedNode getTarget() {
        return target;
    }

    public String getType() {
        return arc.getType();
    }

    @Override
    public boolean isDirected() {
        return arc.isDirected();
    }

}
