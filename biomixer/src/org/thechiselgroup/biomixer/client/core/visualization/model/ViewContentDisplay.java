/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.core.visualization.model;

import org.thechiselgroup.biomixer.client.core.persistence.Persistable;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.ui.SidePanelSection;
import org.thechiselgroup.biomixer.client.core.util.Adaptable;
import org.thechiselgroup.biomixer.client.core.util.Disposable;
import org.thechiselgroup.biomixer.client.core.visualization.behaviors.CompositeVisualItemBehavior;
import org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.GraphDisplayController;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer.VisualItemBehaviorFactory;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface for generic visualizations that can be displayed in a
 * {@link VisualizationModel}.
 * 
 * @author Lars Grammel
 * 
 * @see VisualizationModel
 * @see VisualItem
 */
// TODO rename to visualization display or visualization renderer
// NOTE: 3 main items: slots, properties, functionality interfaces
// NOTE: has lifecycle (which should be described)
public interface ViewContentDisplay extends VisualItemRenderer, IsWidget,
        Disposable, Persistable, Adaptable {

    public enum State {

        CREATED, INITIALIZING, INITIALIZED, DISPOSING, DISPOSED;

    }

    void endRestore();

    /**
     * Returns a descriptive name of the visualization component, e.g.
     * 'Scatterplot'. The name will be used to generate visualization
     * descriptions.
     */
    String getName();

    /**
     * Returns the current value of the property.
     * 
     * @see #setPropertyValue(String, Object)
     */
    <T> T getPropertyValue(String property);

    /**
     * @return {@link SidePanelSection}s for configuring this view content
     *         display.
     */
    // XXX view content displays should not expose side panel sections
    // instead they should provide interfaces they can be adapted to
    SidePanelSection[] getSidePanelSections();

    /**
     * @return {@link Slot}s that are supported by this view content display.
     */
    Slot[] getSlots();

    /**
     * Given a resource set (to be loaded), see if it fails to meet any
     * restrictions of the underlying {@link GraphDisplayController}.
     * 
     * @param resourceSet
     * @return
     */
    boolean validateDataTypes(ResourceSet resourceSet);

    /**
     * Sets callback objects.
     */
    void init(VisualItemContainer container, ViewContentDisplayCallback callback);

    boolean isReady();

    <T> void setPropertyValue(String property, T value);

    /**
     * Sets the size of the content, not including decorations such as border,
     * margin, and padding (and thus different from offset width and height).
     * 
     * @param width
     *            width in pixels
     * @param height
     *            height in pixels
     */
    void setSize(int width, int height);

    void startRestore();

    /**
     * Used for customizing the visual item interactions. The provided factory
     * contains the options available from the {@link ViewWindowContentProducer}
     * context. Individual {@link ViewContentDisplay} classes can make use of
     * those as appropriate.
     * 
     * @param behaviorFactory
     * @return
     */
    CompositeVisualItemBehavior createVisualItemBehaviors(
            VisualItemBehaviorFactory behaviorFactory);

}