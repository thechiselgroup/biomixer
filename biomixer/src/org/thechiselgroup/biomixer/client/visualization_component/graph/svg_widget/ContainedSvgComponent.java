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

import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;

/**
 * A class for handling composite SVG elements which have sub-components grouped
 * inside them. For example, a rectangle with text in it might be surrounded by
 * a container element. That way, both elements can be moved at once, have an
 * event handler, etc.
 * 
 * @author drusk
 * 
 */
public class ContainedSvgComponent {

    private SvgElement container;

    public ContainedSvgComponent(SvgElement container) {
        this.container = container;
    }

    /**
     * Creates a ContainedSvgComponent from an existing one, but using a
     * different container.
     * 
     * @param newContainer
     *            An SVG element which will act as the new container
     * @param other
     *            An existing ContainedSvgComponent whose children will be added
     *            to the newContainer
     * @param newContainerHandler
     *            The event handler to be set on the new container
     */
    public ContainedSvgComponent(SvgElement newContainer,
            ContainedSvgComponent other, ChooselEventHandler newContainerHandler) {
        this.container = newContainer;
        container.setEventListener(newContainerHandler);
        for (int i = 0; i < other.getChildCount(); i++) {
            appendChild(other.getChild(i));
        }
    }

    public void appendChild(ContainedSvgComponent containedSvgComponent) {
        appendChild(containedSvgComponent.asSvg());
    }

    public void appendChild(SvgElement svgElement) {
        container.appendChild(svgElement);
    }

    public SvgElement asSvg() {
        return container;
    }

    public SvgElement getChild(int index) {
        return container.getChild(index);
    }

    public int getChildCount() {
        return container.getChildCount();
    }

    public SvgElement getContainer() {
        return container;
    }

    public void insertAtIndex(int index, SvgElement svgElement) {
        container.insertBefore(svgElement, container.getChild(index));
    }

    public void removeChild(ContainedSvgComponent containedSvgComponent) {
        removeChild(containedSvgComponent.asSvg());
    }

    public void removeChild(String id) {
        container.removeChild(id);
    }

    public void removeChild(SvgElement svgElement) {
        container.removeChild(svgElement);
    }

    public void setEventListener(ChooselEventHandler handler) {
        container.setEventListener(handler);
    }
}
