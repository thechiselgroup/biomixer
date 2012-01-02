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
package org.thechiselgroup.biomixer.client.core.ui;

import com.google.gwt.user.client.ui.Widget;

/**
 * A section in the side panel of the view. Has a title and a content widget.
 * 
 * @author Lars Grammel
 */
public class SidePanelSection {

    private Widget widget;

    private String sectionTitle;

    public SidePanelSection(String sectionTitle, Widget widget) {
        assert sectionTitle != null;
        assert widget != null;

        this.widget = widget;
        this.sectionTitle = sectionTitle;
    }

    public Widget getWidget() {
        return widget;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

}
