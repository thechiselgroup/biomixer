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
package org.thechiselgroup.biomixer.client;

import org.thechiselgroup.biomixer.client.core.ui.Colors;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.visualization.DefaultViewContentDisplaysConfigurationProvider;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem.Subset;
import org.thechiselgroup.biomixer.client.core.visualization.model.initialization.ViewContentDisplayConfiguration;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.FixedValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.VisualItemStatusResolver;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.VisualItemStatusResolver.StatusRule;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphViewContentDisplayFactory;
import org.thechiselgroup.biomixer.client.visualization_component.text.TextViewContentDisplayFactory;
import org.thechiselgroup.biomixer.client.visualization_component.timeline.TimeLine;
import org.thechiselgroup.biomixer.client.visualization_component.timeline.TimeLineViewContentDisplayFactory;

import com.google.inject.Inject;

public class BioMixerWorkbenchViewContentDisplaysConfigurationProvider extends
        DefaultViewContentDisplaysConfigurationProvider {

    @Inject
    public void graph(GraphViewContentDisplayFactory factory) {
        add(factory);
    }

    @Inject
    public void text(TextViewContentDisplayFactory factory) {
        add(factory);
    }

    @Inject
    public void timeLine(TimeLineViewContentDisplayFactory factory) {
        ViewContentDisplayConfiguration configuration = new ViewContentDisplayConfiguration(
                factory);

        configuration.setSlotResolver(TimeLine.BORDER_COLOR,
                new FixedValueResolver(Colors.STEELBLUE_C, DataType.COLOR));
        configuration.setSlotResolver(
                TimeLine.COLOR,
                new VisualItemStatusResolver(Colors.STEELBLUE_C.alpha(0.6),
                        StatusRule.fullOrPartial(Colors.YELLOW_C,
                                Subset.HIGHLIGHTED), StatusRule.fullOrPartial(
                                Colors.ORANGE_C, Subset.SELECTED)));

        add(configuration);
    }

}