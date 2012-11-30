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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.thechiselgroup.biomixer.client.DataTypeValidator;
import org.thechiselgroup.biomixer.client.core.ui.SidePanelSection;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionUtils;
import org.thechiselgroup.biomixer.client.core.util.collections.Delta;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.math.MathUtils;
import org.thechiselgroup.biomixer.client.core.util.math.NumberArray;
import org.thechiselgroup.biomixer.client.core.visualization.behaviors.CompositeVisualItemBehavior;
import org.thechiselgroup.biomixer.client.core.visualization.model.AbstractViewContentDisplay;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer.VisualItemBehaviorFactory;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

// XXX memento not implemented
// XXX order does not update when description property changes
public class TextVisualization extends AbstractViewContentDisplay {

    public final static Slot LABEL_SLOT = new Slot("label", "Label",
            DataType.TEXT);

    public static final Slot FONT_SIZE_SLOT = new Slot("font-size",
            "Font Size", DataType.NUMBER);

    private static final String CSS_TAG_CLOUD = "choosel-TextViewContentDisplay-TagCloud";

    private List<TextItem> items = new ArrayList<TextItem>();

    public static final String CSS_LIST_VIEW_SCROLLBAR = "listViewScrollbar";

    private final TextItemContainer textItemContainer;

    private DoubleToGroupValueMapper<String> groupValueMapper;

    private boolean tagCloud = true;

    private Comparator<TextItem> comparator = new Comparator<TextItem>() {
        @Override
        public int compare(TextItem o1, TextItem o2) {
            return o1.getLabel().getText()
                    .compareToIgnoreCase(o2.getLabel().getText());
        }
    };

    public TextVisualization(DataTypeValidator dataTypeValidator) {
        this(new DefaultTextItemContainer(), dataTypeValidator);
    }

    // for test: can change container
    protected TextVisualization(TextItemContainer textItemContainer,
            DataTypeValidator dataValidator) {
        super(dataValidator);
        assert textItemContainer != null;

        this.textItemContainer = textItemContainer;

        initGroupValueMapper();
    }

    /**
     * <p>
     * Creates TextItems for the added resource items and adds them to the user
     * interface.
     * </p>
     * <p>
     * <b>PERFORMANCE NOTE</b>: This method is designed such that the items are
     * only sorted once, and then there is just a single pass along the sorted
     * items.
     * </p>
     */
    private void addVisualItems(
            LightweightCollection<VisualItem> addedVisualItems) {

        assert addedVisualItems != null;

        // PERFORMANCE: do not execute sort if nothing changes
        if (addedVisualItems.isEmpty()) {
            return;
        }

        for (VisualItem visualItem : addedVisualItems) {
            TextItem textItem = createTextItem(visualItem);
            items.add(textItem);
        }

        // Time complexity: O(n*log(n)).
        Collections.sort(items, comparator);

        /*
         * Time complexity: O(n). Iterate over items and check for addedToPanel
         * flag to prevent IndexOutOfBoundsExceptions and keep execution time
         * linear to number of ResourceItems in this view.
         */
        for (int i = 0; i < items.size(); i++) {
            TextItem textItem = items.get(i);
            if (!textItem.isAddedToPanel()) {
                textItemContainer.insert(textItem.getLabel(), i);
                textItem.updateContent();
                textItem.updateStatusStyling();
                textItem.setAddedToPanel(true);
            }
        }
    }

    private void applyTagCloudCSS(boolean tagCloud) {
        if (tagCloud) {
            textItemContainer.addStyleName(CSS_TAG_CLOUD);
        } else {
            textItemContainer.removeStyleName(CSS_TAG_CLOUD);
        }
    }

    private TextItem createTextItem(VisualItem visualItem) {
        TextItem textItem = new TextItem(visualItem);

        TextItemLabel label = textItemContainer.createTextItemLabel(textItem
                .getResourceItem());

        textItem.init(label);

        visualItem.setDisplayObject(textItem);

        return textItem;
    }

    @Override
    public Widget createWidget() {
        Widget widget = textItemContainer.createWidget();
        applyTagCloudCSS(tagCloud);
        return widget;
    }

    @Override
    public String getName() {
        return "Text";
    }

    @Override
    public SidePanelSection[] getSidePanelSections() {
        FlowPanel settingsPanel = new FlowPanel();

        final CheckBox oneItemPerRowBox = new CheckBox("One item per row");
        oneItemPerRowBox
                .addValueChangeHandler(new ValueChangeHandler<Boolean>() {

                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        setTagCloud(!oneItemPerRowBox.getValue());
                    }

                });
        settingsPanel.add(oneItemPerRowBox);
        oneItemPerRowBox.setValue(!tagCloud);

        return new SidePanelSection[] { new SidePanelSection("Settings",
                settingsPanel), };
    }

    @Override
    public Slot[] getSlots() {
        return new Slot[] { LABEL_SLOT, FONT_SIZE_SLOT };
    }

    private void initGroupValueMapper() {
        groupValueMapper = new DoubleToGroupValueMapper<String>(
                new EquidistantBinBoundaryCalculator(), CollectionUtils.toList(
                        "10px", "14px", "18px", "22px", "26px"));
    }

    private void removeTextItem(TextItem textItem) {
        /*
         * whole row needs to be removed, otherwise lots of empty rows consume
         * the whitespace
         */
        TextItemLabel label = textItem.getLabel();
        items.remove(textItem);
        textItemContainer.remove(label);
    }

    public void setTagCloud(boolean tagCloud) {
        if (tagCloud == this.tagCloud) {
            return;
        }

        this.tagCloud = tagCloud;

        applyTagCloudCSS(tagCloud);
    }

    @Override
    public void update(Delta<VisualItem> delta,
            LightweightCollection<Slot> updatedSlots) {

        addVisualItems(delta.getAddedElements());

        for (VisualItem visualItem : delta.getUpdatedElements()) {
            TextItem textItem = visualItem.<TextItem> getDisplayObject();
            textItem.updateContent();
            textItem.updateStatusStyling();
        }

        for (VisualItem visualItem : delta.getRemovedElements()) {
            removeTextItem(visualItem.<TextItem> getDisplayObject());
        }

        if (!updatedSlots.isEmpty()) {
            for (VisualItem visualItem : getVisualItems()) {
                visualItem.<TextItem> getDisplayObject().updateContent();
            }

        }

        if (!items.isEmpty()) {
            updateFontSizes();
        }
    }

    private void updateFontSizes() {
        assert !items.isEmpty();

        NumberArray fontSizeValues = MathUtils.createNumberArray();

        boolean onlyOneValue = true;
        boolean first = true;
        double firstValue = 0;
        for (TextItem textItem : items) {
            double itemValue = textItem.getFontSizeValue();

            if (first) {
                first = false;
                firstValue = itemValue;
            } else if (firstValue != itemValue) {
                onlyOneValue = false;
            }

            fontSizeValues.push(itemValue);
        }

        if (!onlyOneValue) {
            groupValueMapper.setNumberValues(fontSizeValues);
            for (TextItem textItem : items) {
                textItem.scaleFont(groupValueMapper);
            }
        } else {
            for (TextItem textItem : items) {
                textItem.setFontSize("12px");
            }
        }
    }

    @Override
    public CompositeVisualItemBehavior createVisualItemBehaviors(
            VisualItemBehaviorFactory behaviorFactory) {
        CompositeVisualItemBehavior composite = behaviorFactory
                .createEmptyCompositeVisualItemBehavior();

        composite.add(behaviorFactory
                .createDefaultHighlightingVisualItemBehavior());

        composite.add(behaviorFactory.createDefaultDragVisualItemBehavior());

        composite.add(behaviorFactory
                .createDefaultPopupWithHighlightingVisualItemBehavior());

        composite.add(behaviorFactory
                .createDefaultSwitchSelectionVisualItemBehavior());

        return composite;
    }
}
