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

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class DefaultTextItemContainer implements TextItemContainer {

    private ScrollPanel scrollPanel;

    private FlowPanel itemPanel;

    public DefaultTextItemContainer() {
        itemPanel = new FlowPanel();

        scrollPanel = new ScrollPanel(itemPanel);
        scrollPanel.addStyleName(TextVisualization.CSS_LIST_VIEW_SCROLLBAR);
    }

    @Override
    public void addStyleName(String cssClass) {
        itemPanel.addStyleName(cssClass);
    }

    @Override
    public TextItemLabel createTextItemLabel(VisualItem resourceItem) {
        return new DefaultTextItemLabel();
    }

    @Override
    public Widget createWidget() {
        return scrollPanel;
    }

    @Override
    public void insert(TextItemLabel label, int row) {
        Element element = itemPanel.getElement();
        if (row == 0) {
            element.appendChild(label.getElement());
        } else {
            element.insertAfter(label.getElement(), element.getChild(row - 1));
        }
    }

    @Override
    public void remove(TextItemLabel itemLabel) {
        itemLabel.getElement().removeFromParent();
    }

    @Override
    public void removeStyleName(String cssClass) {
        itemPanel.removeStyleName(cssClass);
    }

}