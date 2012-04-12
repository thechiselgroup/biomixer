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
package org.thechiselgroup.biomixer.client.core.util.text;

import java.util.Collection;
import java.util.Map;

import org.thechiselgroup.biomixer.client.core.geometry.SizeInt;
import org.thechiselgroup.biomixer.client.core.ui.CSS;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;

import com.google.gwt.user.client.Element;

public class HtmlTextBoundsEstimator extends BoundsEstimator implements
        TextBoundsEstimator {

    public HtmlTextBoundsEstimator() {
    }

    public HtmlTextBoundsEstimator(String fontFamily, String fontStyle,
            String fontWeight, String fontSize) {

        configureFontFamily(fontFamily);
        configureFontStyle(fontStyle);
        configureFontWeight(fontWeight);
        configureFontSize(fontSize);
    }

    @Override
    public void configureFontFamily(String fontFamily) {
        CSS.setFontFamily(estimatorElement, fontFamily);
    }

    public void configureFontSettings(Element prototype) {
        configureFontFamily(CSS.getComputedStyle(prototype, CSS.FONT_FAMILY));
        configureFontStyle(CSS.getComputedStyle(prototype, CSS.FONT_STYLE));
        configureFontWeight(CSS.getComputedStyle(prototype, CSS.FONT_WEIGHT));
        configureFontSize(CSS.getComputedStyle(prototype, CSS.FONT_SIZE));
    }

    @Override
    public void configureFontSize(String fontSize) {
        CSS.setFontSize(estimatorElement, fontSize);
    }

    @Override
    public void configureFontStyle(String fontStyle) {
        CSS.setFontStyle(estimatorElement, fontStyle);
    }

    @Override
    public void configureFontWeight(String fontWeight) {
        CSS.setFontWeight(estimatorElement, fontWeight);
    }

    @Override
    public int getHeight(String text) {
        setText(text);
        return getHeight();
    }

    @Override
    public SizeInt getSize(String text) {
        setText(text);
        return getSize();
    }

    @Override
    public int getWidth(String text) {
        setText(text);
        return getWidth();
    }

    public int getWidth(String text, Element prototype) {
        configureFontSettings(prototype);
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

    private void setText(String text) {
        estimatorElement.setInnerText(text);
    }

}
