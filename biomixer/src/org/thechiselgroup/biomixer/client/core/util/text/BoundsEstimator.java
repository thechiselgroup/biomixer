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

import org.thechiselgroup.biomixer.client.core.geometry.DefaultSizeInt;
import org.thechiselgroup.biomixer.client.core.geometry.SizeInt;
import org.thechiselgroup.biomixer.client.core.ui.CSS;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RootPanel;

// TODO fix usage
// TODO add documentation -- setup, teardown
public class BoundsEstimator {

    protected final Element estimatorElement;

    private Element rootElement;

    public BoundsEstimator() {
        this(RootPanel.get().getElement());
    }

    public BoundsEstimator(Element rootElement) {
        this.estimatorElement = createEstimatorElement();
        this.rootElement = rootElement;
    }

    private Element createEstimatorElement() {
        Element estimatorElement = DOM.createSpan();

        CSS.setPosition(estimatorElement, CSS.ABSOLUTE);
        CSS.setLeft(estimatorElement, -100);
        CSS.setTop(estimatorElement, -100);
        CSS.setZIndex(estimatorElement, -1000);
        CSS.setBorder(estimatorElement, "0px none");
        CSS.setPadding(estimatorElement, 0);
        CSS.setMargin(estimatorElement, 0);

        return estimatorElement;
    }

    public int getHeight() {
        return estimatorElement.getOffsetHeight();
    }

    public SizeInt getSize() {
        int width = estimatorElement.getOffsetWidth();
        int height = estimatorElement.getOffsetHeight();
        return new DefaultSizeInt(width, height);
    }

    public int getWidth() {
        return estimatorElement.getOffsetWidth();
    }

    public void setUp() {
        rootElement.appendChild(estimatorElement);
    }

    public void tearDown() {
        rootElement.removeChild(estimatorElement);
    }

}