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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.nodes;

import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.AbstractRenderedNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.IsSvg;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

/**
 * All SVG based rendered nodes should extend this.
 * 
 * @author drusk
 * 
 */
public abstract class AbstractSvgRenderedNode extends AbstractRenderedNode
        implements IsSvg {

    protected AbstractSvgRenderedNode(Node node) {
        super(node);
    }

}
