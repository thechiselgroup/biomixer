/*******************************************************************************
 * Copyright 2012 Lars Grammel 
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
package org.thechiselgroup.biomixer.shared.svg;

import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEvent;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;

/**
 * SVG element, e.g. rect, g, svg etc.
 * 
 * @author Lars Grammel
 */
public interface SvgElement {

    /**
     * Adds {@code newChild} to the end of the list of children of this SVG
     * element. If {@code newChild} is already in the tree, it is first removed.
     * 
     * @param newChild
     *            {@link SvgElement} to add
     * @return Added SVG element.
     */
    SvgElement appendChild(SvgElement newChild);

    String getAttributeAsString(String attributeName);

    /**
     * @return BBox of this SVG element.
     */
    SizeDouble getBBox();

    /**
     * Gets the child SVG element at the given index.
     * 
     * @param childIndex
     *            the index of the element to be retrieved
     * @return the child element at the given index
     */
    SvgElement getChild(int childIndex);

    /**
     * Gets the number of child nodes contained within this node.
     * 
     * @return the number of child nodes
     */
    int getChildCount();

    /**
     * Gets this element's {@link SvgStyle} object.
     */
    SvgStyle getStyle();

    /**
     * Inserts the node newChild before the existing child node refChild. If
     * refChild is <code>null</code>, insert newChild at the end of the list of
     * children.
     * 
     * @param newChild
     *            The node to insert
     * @param refChild
     *            The reference node (that is, the node before which the new
     *            node must be inserted), or <code>null</code>
     * @return The node being inserted
     */
    SvgElement insertBefore(SvgElement newChild, SvgElement refChild);

    // TODO document
    void removeAllChildren();

    /**
     * Removes the child node indicated by oldChild from the list of children,
     * and returns it.
     * 
     * @param oldChild
     *            The node being removed
     * @return The node removed
     */
    SvgElement removeChild(SvgElement oldChild);

    /**
     * Adds a new attribute. If an attribute with that name is already present
     * in the element, its value is changed to be that of the value parameter.
     * 
     * @param name
     *            The name of the attribute to create or alter
     * @param value
     *            Value to set in string form
     */
    void setAttribute(String attribute, double value);

    /**
     * Adds a new attribute. If an attribute with that name is already present
     * in the element, its value is changed to be that of the value parameter.
     * 
     * @param name
     *            The name of the attribute to create or alter
     * @param value
     *            Value to set in string form
     */
    void setAttribute(String attribute, String value);

    /**
     * Sets the {@link ChooselEventHandler} to receive events for the given
     * element. Only one such listener may exist for a single element.
     * 
     * @param handler
     *            the handler to receive {@link ChooselEvent events}
     */
    void setEventListener(ChooselEventHandler handler);

    /**
     * Sets the text content of a SVG element that is capable of having a text
     * content, e.g. 'text'.
     */
    void setTextContent(String text);

}