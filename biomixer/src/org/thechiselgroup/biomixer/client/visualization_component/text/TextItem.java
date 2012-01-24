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
package org.thechiselgroup.biomixer.client.visualization_component.text;

import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem.Subset;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemInteraction;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

public class TextItem {

    public static final String CSS_HIGHLIGHTED = "textItemHighlighted";

    public static final String CSS_PARTIALLY_HIGHLIGHTED = "textItemPartiallyHighlighted";

    public static final String CSS_LIST = "textItem";

    public static final String CSS_SELECTED = "textItemSelected";

    private TextItemLabel label;

    private VisualItem visualItem;

    /**
     * Flag that marks if the label of this text item has already been added to
     * the container panel. Used to increase the performance of adding multiple
     * text items to the view.
     */
    private boolean addedToPanel = false;

    private String lastFontSize;

    private String cachedDescription;

    public TextItem(VisualItem visualItem) {
        assert visualItem != null;

        this.visualItem = visualItem;
    }

    public double getFontSizeValue() {
        return ((Number) visualItem.getValue(TextVisualization.FONT_SIZE_SLOT))
                .doubleValue();
    }

    public TextItemLabel getLabel() {
        return label;
    }

    public String getLabelValue() {
        return (String) visualItem.getValue(TextVisualization.LABEL_SLOT);
    }

    public VisualItem getResourceItem() {
        return visualItem;
    }

    public void init(TextItemLabel label) {
        this.label = label;

        label.registerHandler(new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                visualItem.reportInteraction(new VisualItemInteraction(event));
            }
        });
        label.addStyleName(CSS_LIST);

        updateContent();
    }

    public boolean isAddedToPanel() {
        return addedToPanel;
    }

    /**
     * <p>
     * <b>IMPLEMENTATION NOTE</b>: the last calculated font size gets cached and
     * is compared to the result of the current font size calculation to prevent
     * expensive DOM styling operations.
     * </p>
     */
    public void scaleFont(DoubleToGroupValueMapper<String> groupValueMapper) {
        String newFontSizeLabelValue = groupValueMapper
                .getGroupValue(getFontSizeValue());

        setFontSize(newFontSizeLabelValue);
    }

    public void setAddedToPanel(boolean addedToPanel) {
        this.addedToPanel = addedToPanel;
    }

    public void setFontSize(String newFontSize) {
        if (lastFontSize == null || newFontSize.compareTo(lastFontSize) != 0) {
            label.setFontSize(newFontSize);
            lastFontSize = newFontSize;
        }
    }

    public void updateContent() {
        // TODO what is this for
        if (label == null) {
            return;
        }

        /*
         * PERFORMANCE: cache description and font size and only update UI
         * elements when there is a change. This makes a huge difference with
         * several thousand text items.
         */
        String description = getLabelValue();

        if (cachedDescription == null || !cachedDescription.equals(description)) {
            label.setText(description);
            cachedDescription = description;
        }
    }

    public void updateStatusStyling() {
        switch (visualItem.getStatus(Subset.HIGHLIGHTED)) {
        case FULL: {
            label.addStyleName(CSS_HIGHLIGHTED);
            label.removeStyleName(CSS_PARTIALLY_HIGHLIGHTED);
        }
            break;
        case PARTIAL: {
            label.removeStyleName(CSS_HIGHLIGHTED);
            label.addStyleName(CSS_PARTIALLY_HIGHLIGHTED);
        }
            break;
        case NONE: {
            label.removeStyleName(CSS_HIGHLIGHTED);
            label.removeStyleName(CSS_PARTIALLY_HIGHLIGHTED);
        }
            break;
        }

        switch (visualItem.getStatus(Subset.SELECTED)) {
        case FULL: {
            label.addStyleName(CSS_SELECTED);
        }
            break;
        case PARTIAL: {
            label.addStyleName(CSS_SELECTED);
        }
            break;
        case NONE: {
            label.removeStyleName(CSS_SELECTED);
        }
            break;
        }
    }

}