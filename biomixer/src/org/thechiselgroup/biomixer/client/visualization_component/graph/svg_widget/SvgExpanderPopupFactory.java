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
package org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionUtils;
import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;
import org.thechiselgroup.biomixer.shared.svg.SvgUtils;

public class SvgExpanderPopupFactory {

    private final SvgElementFactory svgElementFactory;

    private BoxedTextSvgFactory boxedTextFactory;

    public SvgExpanderPopupFactory(SvgElementFactory svgElementFactory,
            TextBoundsEstimator textBoundsEstimator) {
        assert svgElementFactory != null;
        assert textBoundsEstimator != null;
        this.svgElementFactory = svgElementFactory;
        this.boxedTextFactory = new BoxedTextSvgFactory(svgElementFactory,
                textBoundsEstimator);
    }

    public SvgPopupExpanders createExpanderPopupList(
            PointDouble topLeftLocation, Set<String> expanderIds) {

        SvgElement popUpContainer = svgElementFactory.createElement(Svg.SVG);
        SvgUtils.setXY(popUpContainer, topLeftLocation);

        Map<String, BoxedTextSvgElement> boxedTextEntries = CollectionFactory
                .createStringMap();

        double maxWidth = 0.0;
        List<String> sortedExpanderIds = CollectionUtils
                .asSortedList(expanderIds);
        for (String expanderId : sortedExpanderIds) {
            BoxedTextSvgElement boxedText = boxedTextFactory
                    .createBoxedText(expanderId);
            boxedTextEntries.put(expanderId, boxedText);
            double boxWidth = boxedText.getTotalWidth();
            if (boxWidth > maxWidth) {
                maxWidth = boxWidth;
            }
        }

        double currentOffsetY = 0;
        for (String expanderId : sortedExpanderIds) {
            BoxedTextSvgElement boxedText = boxedTextEntries.get(expanderId);
            boxedText.setBoxWidth(maxWidth);
            boxedText.setY(currentOffsetY);
            popUpContainer.appendChild(boxedText.getContainer());
            currentOffsetY += boxedText.getTotalHeight();
        }

        return new SvgPopupExpanders(popUpContainer, boxedTextEntries);
    }
}
