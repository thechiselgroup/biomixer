/*******************************************************************************
 * Copyright 2012 David Rusk 
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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.expanders;

import java.util.Map;

import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNodeExpander;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.IsSvg;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.nodes.SvgBoxedText;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.text_renderer.TextSvgElement;

public class BoxedTextSvgNodeExpander implements RenderedNodeExpander, IsSvg {

    private Map<String, SvgBoxedText> expanders;

    private SvgElement container;

    public BoxedTextSvgNodeExpander(SvgElement container,
            Map<String, SvgBoxedText> expanders) {
        this.container = container;
        this.expanders = expanders;
    }

    @Override
    public SvgElement asSvgElement() {
        return container;
    }

    public SvgBoxedText getEntryByExpanderLabel(String expanderLabel) {
        return expanders.get(expanderLabel);
    }

    @Override
    public ChooselEventHandler getEventHandler(String optionId) {
        return ((TextSvgElement) expanders.get(optionId).asSvgElement())
                .getEventListener();
    }

    @Override
    public void setEventHandlerOnOption(String optionId,
            ChooselEventHandler handler) {
        assert expanders.containsKey(optionId) : "Option id " + optionId
                + " is not among those rendered";
        expanders.get(optionId).setEventListener(handler);
    }

    @Override
    public void setOptionBackgroundColor(String optionId, String color) {
        assert expanders.containsKey(optionId) : "Option id " + optionId
                + " is not among those rendered";
        expanders.get(optionId).setBackgroundColor(color);
    }

}
