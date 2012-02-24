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
package org.thechiselgroup.biomixer.shared.svg.text_renderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEvent;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgStyle;

public class TextSvgElement implements SvgElement, SvgStyle {

    private final Map<String, String> attributes = new TreeMap<String, String>();

    private final Map<String, String> cssAttributes = new TreeMap<String, String>();

    private final String tagName;

    private final List<SvgElement> children = new ArrayList<SvgElement>();

    private ChooselEventHandler handler;

    private String textContent;

    public TextSvgElement(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public SvgElement appendChild(SvgElement element) {
        assert element != null;
        children.add(element);
        return element;
    }

    public void fireEvent(ChooselEvent chooselEvent) {
        assert handler != null : "no event handler set";
        handler.onEvent(chooselEvent);
    }

    @Override
    public String getAttributeAsString(String attributeName) {
        return attributes.get(attributeName);
    }

    @Override
    public SizeDouble getBBox() {
        return null; // TODO implement
    }

    @Override
    public TextSvgElement getChild(int childIndex) {
        return (TextSvgElement) children.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    /**
     * 
     * @return returns the registered event handler. Should only be used for
     *         testing purposes.
     */
    public ChooselEventHandler getEventListener() {
        return handler;
    }

    @Override
    public SvgStyle getStyle() {
        return this;
    }

    @Override
    public boolean hasAttribute(String attribute) {
        // XXX check in cssAttributes?
        return attributes.containsKey(attribute);
    }

    @Override
    public SvgElement insertBefore(SvgElement newChild, SvgElement refChild) {
        assert newChild != null;
        assert refChild != null;
        children.add(children.indexOf(refChild), newChild);
        return newChild;
    }

    @Override
    public void removeAllChildren() {
        children.clear();
    }

    @Override
    public void removeAttribute(String attribute) {
        attributes.remove(attribute);
    }

    @Override
    public void removeChild(String id) {
        for (Iterator<SvgElement> it = children.iterator(); it.hasNext();) {
            SvgElement child = it.next();
            if (child.hasAttribute(Svg.ID)
                    && child.getAttributeAsString(Svg.ID).equals(id)) {
                it.remove();
            }
        }
    }

    @Override
    public SvgElement removeChild(SvgElement oldChild) {
        children.remove(oldChild);
        return oldChild;
    }

    @Override
    public void setAttribute(String attribute, double value) {
        attributes.put(attribute, "" + value);
    }

    @Override
    public void setAttribute(String attribute, String value) {
        attributes.put(attribute, value);
    }

    @Override
    public void setEventListener(ChooselEventHandler handler) {
        this.handler = handler;
    }

    @Override
    public void setProperty(String attribute, String value) {
        cssAttributes.put(attribute, value);
    }

    @Override
    public void setTextContent(String text) {
        this.textContent = text;
    }

    // TODO add style setting (CSS attributes)
    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<" + tagName + " ");
        for (Entry<String, String> entry : attributes.entrySet()) {
            sb.append(entry.getKey()).append("=\"").append(entry.getValue())
                    .append("\" ");
        }
        sb.append(">");
        for (SvgElement child : children) {
            assert child != null;
            assert child instanceof TextSvgElement : child
                    + " must be instance of TestSvgElement";
            sb.append(((TextSvgElement) child).toXML());
        }
        if (textContent != null) {
            sb.append(textContent);
        }
        sb.append("</" + tagName + ">");
        return sb.toString();
    }

}