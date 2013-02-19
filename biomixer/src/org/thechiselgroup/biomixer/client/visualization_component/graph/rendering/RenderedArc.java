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

import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;

/**
 * A displayable arc.
 * 
 * @author drusk
 * 
 */
public interface RenderedArc {

    Arc getArc();

    RenderedNode getSource();

    RenderedNode getTarget();

    double getThickness();

    boolean isDirected();

    void setArcStyle(String arcStyle);

    void setArcHead(String arcHead);

    void setColor(String color);

    void setEventListener(ChooselEventHandler handler);

    void setThickness(String thickness);

    void setLabelRendering(boolean newValue);

    boolean getLabelRendering();

    /**
     * Updates the arc to match new source or target node locations.
     */
    void update();

}
