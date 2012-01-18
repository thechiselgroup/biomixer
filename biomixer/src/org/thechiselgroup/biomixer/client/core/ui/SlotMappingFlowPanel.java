package org.thechiselgroup.biomixer.client.core.ui;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class SlotMappingFlowPanel extends FlowPanel {

    private static final String CSS_CONFIGURATION_PANEL_SETTING = "choosel-ConfigurationPanel-Setting";

    private final Label slotMappingLabel;

    private Widget slotMappingWidget;

    /**
     * The label must be set in this method, and cannot be changed afterwards
     */
    public SlotMappingFlowPanel(Label label, Widget widget) {
        assert label != null;
        assert widget != null;

        setStyleName(CSS_CONFIGURATION_PANEL_SETTING);
        this.slotMappingLabel = label;
        this.slotMappingWidget = widget;
        add(label);
        add(widget);
    }

    /**
     * This method updates the contained widget in this class. To ensure the
     * correct order of rendering, it removes both inner widgets and adds them
     * again after the change has occured
     */
    // TODO refactor
    public void setSlotMappingWidget(Widget widget) {
        remove(slotMappingLabel);
        remove(slotMappingWidget);
        this.slotMappingWidget = widget;
        add(slotMappingLabel);
        add(slotMappingWidget);
    }
}
