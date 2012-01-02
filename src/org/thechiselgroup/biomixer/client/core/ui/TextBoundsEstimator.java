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

import java.util.Collection;
import java.util.Map;

import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;

import com.google.gwt.user.client.Element;

public class TextBoundsEstimator extends BoundsEstimator {

    public TextBoundsEstimator() {
    }

    public TextBoundsEstimator(Element prototype) {
        applyFontSettings(prototype);
    }

    public TextBoundsEstimator(String fontFamily, String fontStyle,
            String fontWeight, String fontSize) {
        applyFontSettings(fontFamily, fontStyle, fontWeight, fontSize);
    }

    public void applyFontSettings(Element prototype) {
        String fontFamily = CSS.getComputedStyle(prototype, CSS.FONT_FAMILY);
        String fontStyle = CSS.getComputedStyle(prototype, CSS.FONT_STYLE);
        String fontWeight = CSS.getComputedStyle(prototype, CSS.FONT_WEIGHT);
        String fontSize = CSS.getComputedStyle(prototype, CSS.FONT_SIZE);

        applyFontSettings(fontFamily, fontStyle, fontWeight, fontSize);
    }

    public void applyFontSettings(String fontFamily, String fontStyle,
            String fontWeight, String fontSize) {

        CSS.setFontFamily(estimatorElement, fontFamily);
        CSS.setFontStyle(estimatorElement, fontStyle);
        CSS.setFontWeight(estimatorElement, fontWeight);
        CSS.setFontSize(estimatorElement, fontSize);
    }

    public int getWidth(String text) {
        setText(text);
        return getWidth();
    }

    public int getWidth(String text, Element prototype) {
        applyFontSettings(prototype);
        return getWidth(text);
    }

    /**
     * Calculates the text widths for the texts and returns a mapping of the
     * texts to their widths.
     */
    public Map<String, Integer> getWidths(Collection<String> texts) {
        assert texts != null;

        Map<String, Integer> result = CollectionFactory.createStringMap();
        for (String value : texts) {
            setText(value);
            result.put(value, getWidth());
        }
        return result;
    }

    public void setText(String text) {
        estimatorElement.setInnerText(text);
    }

}
