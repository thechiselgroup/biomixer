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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg;

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
public class CompositeSvgComponent implements IsSvg {

    protected SvgElement compositeElement;

    public CompositeSvgComponent(SvgElement container) {
        this.compositeElement = container;
    }

    /**
     * Creates a <code>CompositeSvgComponent</code> from an existing one, but
     * using a different container.
     * 
     * @param newContainer
     *            An SVG element which will act as the new container
     * @param other
     *            An existing CompositeSvgComponent whose children will be added
     *            to the newContainer
     * @param newContainerHandler
     *            The event handler to be set on the new container
     */
    public CompositeSvgComponent(SvgElement newContainer,
            CompositeSvgComponent other, ChooselEventHandler newContainerHandler) {
        this.compositeElement = newContainer;
        compositeElement.setEventListener(newContainerHandler);
        while (other.asSvgElement().getChildCount() > 0) {
            /*
             * appendChild to newContainer seems to remove it from the old
             * container, therefore ordinary for loop doesn't work because
             * other.childCount() is changing
             */
            appendChild(other.asSvgElement().getChild(0));
        }
    }

    public void appendChild(IsSvg child) {
        appendChild(child.asSvgElement());
    }

    private void appendChild(SvgElement svgElement) {
        compositeElement.appendChild(svgElement);
    }

    @Override
    public SvgElement asSvgElement() {
        return compositeElement;
    }

    public void removeAllChildren() {
        compositeElement.removeAllChildren();
    }

    public void removeChild(IsSvg child) {
        compositeElement.removeChild(child.asSvgElement());
    }

    public void setEventListener(ChooselEventHandler handler) {
        compositeElement.setEventListener(handler);
    }

}
