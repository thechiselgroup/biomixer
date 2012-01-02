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

import org.thechiselgroup.choosel.core.client.ui.Colors;
import org.thechiselgroup.choosel.core.client.util.DataType;
import org.thechiselgroup.choosel.core.client.visualization.DefaultViewContentDisplaysConfigurationProvider;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItem.Subset;
import org.thechiselgroup.choosel.core.client.visualization.model.initialization.ViewContentDisplayConfiguration;
import org.thechiselgroup.choosel.core.client.visualization.resolvers.FixedValueResolver;
import org.thechiselgroup.choosel.core.client.visualization.resolvers.SubsetDelegatingValueResolver;
import org.thechiselgroup.choosel.core.client.visualization.resolvers.VisualItemStatusResolver;
import org.thechiselgroup.choosel.core.client.visualization.resolvers.VisualItemStatusResolver.StatusRule;
import org.thechiselgroup.choosel.visualization_component.chart.client.barchart.BarChart;
import org.thechiselgroup.choosel.visualization_component.chart.client.barchart.BarChartViewContentDisplayFactory;
import org.thechiselgroup.choosel.visualization_component.graph.client.GraphViewContentDisplayFactory;
import org.thechiselgroup.choosel.visualization_component.text.client.TextViewContentDisplayFactory;
import org.thechiselgroup.choosel.visualization_component.timeline.client.TimeLine;
import org.thechiselgroup.choosel.visualization_component.timeline.client.TimeLineViewContentDisplayFactory;

import com.google.inject.Inject;

public class BioMixerWorkbenchViewContentDisplaysConfigurationProvider extends
        DefaultViewContentDisplaysConfigurationProvider {

    @Inject
    public void barchart(BarChartViewContentDisplayFactory originalFactory) {
        ViewContentDisplayConfiguration configuration = new ViewContentDisplayConfiguration(
                originalFactory);

        configuration.setSlotResolver(
                BarChart.BAR_COLOR,
                new VisualItemStatusResolver(Colors.STEELBLUE_C, StatusRule
                        .fullOrPartial(Colors.ORANGE_C, Subset.SELECTED)));

        configuration.setSlotResolver(BarChart.BAR_BORDER_COLOR,
                new FixedValueResolver(Colors.STEELBLUE_C, DataType.COLOR));

        configuration.setSlotResolver(BarChart.PARTIAL_BAR_LENGTH,
                new SubsetDelegatingValueResolver(BarChart.BAR_LENGTH,
                        Subset.HIGHLIGHTED));
        configuration.setSlotResolver(BarChart.PARTIAL_BAR_COLOR,
                new FixedValueResolver(Colors.YELLOW_C, DataType.COLOR));
        configuration.setSlotResolver(BarChart.PARTIAL_BAR_BORDER_COLOR,
                new FixedValueResolver(Colors.STEELBLUE_C, DataType.COLOR));

        add(configuration);
    }

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