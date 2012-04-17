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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering;

import java.util.Set;

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

/**
 * Creates the visible UI elements for displaying node expansion options.
 * 
 * @author drusk
 * 
 */
public interface NodeExpanderRenderer {

    /**
     * 
     * @param topLeftLocation
     *            the point where the expander's top left corner should go
     * @param expanderLabels
     *            the labels to be displayed as expansion options
     * @param node
     *            the node for which this expander expands
     * @return the rendered node expander
     */
    RenderedNodeExpander renderNodeExpander(PointDouble topLeftLocation,
            Set<String> expanderLabels, Node node);

}
