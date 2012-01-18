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
package org.thechiselgroup.biomixer.client.visualization_component.text;

import org.thechiselgroup.biomixer.client.core.visualization.model.ViewContentDisplay;
import org.thechiselgroup.biomixer.client.core.visualization.model.initialization.ViewContentDisplayFactory;

public class ListViewContentDisplayFactory implements ViewContentDisplayFactory {

    public static final String ID = "org.thechiselgroup.choosel.visualization_component.text.client.NonTagCloudTextViewContentDisplayFactory";

    @Override
    public ViewContentDisplay createViewContentDisplay() {
        TextVisualization visualization = new TextVisualization();
        visualization.setTagCloud(false);
        return visualization;
    }

    @Override
    public String getViewContentTypeID() {
        return ID;
    }

}
