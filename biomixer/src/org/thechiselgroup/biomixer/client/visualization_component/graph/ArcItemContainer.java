/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.visualization_component.graph;

import java.util.Collection;
import java.util.Map;

import org.thechiselgroup.biomixer.client.core.persistence.Memento;
import org.thechiselgroup.biomixer.client.core.persistence.Persistable;
import org.thechiselgroup.biomixer.client.core.persistence.PersistableRestorationService;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetAccessor;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetCollector;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplay;

public class ArcItemContainer implements Persistable {

    private static final String MEMENTO_ARC_COLOR = "arcColor";

    private static final String MEMENTO_ARC_STYLE = "arcStyle";

    private static final String MEMENTO_VISIBLE = "visible";

    private static final String MEMENTO_ARC_THICKNESS = "arcThicknessLevel";

    private final ArcType arcType;

    private final Map<String, ArcItem> arcItemsById = CollectionFactory
            .createStringMap();

    private final GraphDisplay graphDisplay;

    private String arcColor;

    private String arcStyle;

    /**
     * The override value for arc thickness. If it is equal to 0, then the
     * ArcType default will be used, possibly resulting in different thicknesses
     * per arc, if the ArcType defines those semantics. If it is any other
     * number, then that value will be used for all arcs. It is best to think fo
     * as a level the user has selected for arc thicknesses, rather than being a
     * value of the thickness. This way it is comfortable to always look at the
     * arc thickness as being managed by the arc type, ultimately.
     */
    private int arcThicknessLevel;

    private final VisualItemContainer context;

    private boolean visible;

    public ArcItemContainer(ArcType arcType, GraphDisplay graphDisplay,
            VisualItemContainer context) {

        assert graphDisplay != null;
        assert arcType != null;
        assert context != null;

        this.arcType = arcType;
        this.graphDisplay = graphDisplay;
        this.context = context;

        arcStyle = arcType.getDefaultArcStyle();
        arcColor = arcType.getDefaultArcColor();
        // Set so that we get the ArcType defaults right away
        arcThicknessLevel = 0;

        visible = true;
    }

    public String getArcColor() {
        return arcColor;
    }

    public Collection<ArcItem> getArcItems() {
        return arcItemsById.values();
    }

    public String getArcStyle() {
        return arcStyle;
    }

    public int getArcThickness() {
        return arcThicknessLevel;
    }

    public ArcType getArcType() {
        return arcType;
    }

    public void removeVisualItem(VisualItem visualItem) {
        assert visualItem != null;

        LightweightCollection<Arc> arcs = arcType.getArcs(visualItem, context);
        for (Arc arc : arcs) {
            String arcId = arc.getId();
            if (arcItemsById.containsKey(arcId)) {
                arcItemsById.get(arcId).setVisible(false);
                arcItemsById.remove(arcId);
            }
        }

    }

    @Override
    public void restore(Memento state,
            PersistableRestorationService restorationService,
            ResourceSetAccessor accessor) {

        setVisible((Boolean) state.getValue(MEMENTO_VISIBLE));
        setArcColor((String) state.getValue(MEMENTO_ARC_COLOR));
        setArcStyle((String) state.getValue(MEMENTO_ARC_STYLE));
        setArcThicknessLevel((Integer) state.getValue(MEMENTO_ARC_THICKNESS));
    }

    @Override
    public Memento save(ResourceSetCollector resourceSetCollector) {
        Memento memento = new Memento();

        memento.setValue(MEMENTO_VISIBLE, visible);
        memento.setValue(MEMENTO_ARC_THICKNESS, arcThicknessLevel);
        memento.setValue(MEMENTO_ARC_COLOR, arcColor);
        memento.setValue(MEMENTO_ARC_STYLE, arcStyle);

        return memento;
    }

    public void setArcColor(String arcColor) {
        assert arcColor != null;

        this.arcColor = arcColor;
        for (ArcItem arcItem : getArcItems()) {
            arcItem.setColor(arcColor);
        }
    }

    public void setArcStyle(String arcStyle) {
        assert arcStyle != null;

        this.arcStyle = arcStyle;
        for (ArcItem arcItem : getArcItems()) {
            arcItem.setArcStyle(arcStyle);
        }
    }

    /**
     * Passing a thickness of 0 or greater, where 0 indicates that the default
     * arc thickness for the associated ArcType should be used. The default
     * thickness might resolve to individual arc thicknesses rather than a
     * single thickness for all arcs of that type.
     * 
     * @param arcThicknessLevel
     */
    public void setArcThicknessLevel(int arcThicknessLevel) {
        assert arcThicknessLevel >= 0;

        this.arcThicknessLevel = arcThicknessLevel;
        for (ArcItem arcItem : getArcItems()) {
            // TODO Should this arc thickness stuff get pushed deeper, into
            // ArcItem?
            arcItem.setArcThickness(this.arcType.getArcThickness(
                    arcItem.getArc(), this.arcThicknessLevel));
        }
    }

    public void setVisible(boolean visible) {
        if (this.visible == visible) {
            return;
        }

        for (ArcItem arcItem : getArcItems()) {
            arcItem.setVisible(visible);
        }

        this.visible = visible;
    }

    public void update(LightweightCollection<VisualItem> visualItems) {
        assert visualItems != null;

        for (VisualItem visualItem : visualItems) {
            update(visualItem);
        }
    }

    private void update(VisualItem visualItem) {
        assert visualItem != null;
        assert context.containsVisualItem(visualItem.getId());
        assert graphDisplay.containsNode(visualItem.getId());

        for (Arc arc : arcType.getArcs(visualItem, context)) {
            // XXX what about changes?
            if (!arcItemsById.containsKey(arc.getId())
                    && graphDisplay.containsNode(arc.getSourceNodeId())
                    && graphDisplay.containsNode(arc.getTargetNodeId())) {
                ArcItem arcItem = new ArcItem(arc, arcColor, arcStyle,
                        arcType.getArcThickness(arc, this.arcThicknessLevel),
                        graphDisplay);
                arcItem.setVisible(visible);

                arcItemsById.put(arc.getId(), arcItem);
            }
        }

    }
}
