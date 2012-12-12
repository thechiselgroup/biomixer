/*******************************************************************************
 * Copyright 2012 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.core.visualization;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class LeftViewTopBarExtension extends AbstractIsWidgetTopBarExtension {

    public LeftViewTopBarExtension(IsWidget widget) {
        super(widget);
    }

    @Override
    protected void addWidgetToTopBar(Widget realWidget, DockPanel topBar) {
        topBar.add(realWidget, DockPanel.WEST);
        topBar.setCellHorizontalAlignment(realWidget, HasAlignment.ALIGN_LEFT);
    }

}