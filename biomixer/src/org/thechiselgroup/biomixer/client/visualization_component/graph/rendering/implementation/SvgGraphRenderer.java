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

import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.ArcRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.GraphRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.NodeRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

/**
 * Constructs graph visualization elements using SVG.
 * 
 * @author drusk
 * 
 */
public class SvgGraphRenderer implements GraphRenderer {

    private NodeRenderer nodeRenderer;

    private ArcRenderer arcRenderer;

    public SvgGraphRenderer(NodeRenderer nodeRenderer, ArcRenderer arcRenderer) {
        this.nodeRenderer = nodeRenderer;
        this.arcRenderer = arcRenderer;
    }

    @Override
    public void removeArc(Arc arc) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeNode(Node node) {
        // TODO Auto-generated method stub

    }

    @Override
    public void renderArc(Arc arc) {
        // TODO Auto-generated method stub

    }

    @Override
    public void renderNode(Node node) {
        // TODO Auto-generated method stub

    }

}
