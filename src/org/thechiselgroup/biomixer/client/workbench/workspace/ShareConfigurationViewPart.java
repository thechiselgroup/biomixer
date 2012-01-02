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
package org.thechiselgroup.biomixer.client.workbench.workspace;

import org.thechiselgroup.biomixer.client.core.ui.SidePanelSection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.core.visualization.ViewPart;

public class ShareConfigurationViewPart implements ViewPart {

    private ShareConfiguration shareConfiguration;

    public ShareConfigurationViewPart(ShareConfiguration shareConfiguration) {
        this.shareConfiguration = shareConfiguration;
    }

    @Override
    public void addSidePanelSections(LightweightList<SidePanelSection> sections) {
        for (SidePanelSection section : shareConfiguration
                .getSidePanelSections()) {
            sections.add(section);
        }
    }

    @Override
    public void afterViewCreation(View view) {
        shareConfiguration.setView(view);
    }

}
