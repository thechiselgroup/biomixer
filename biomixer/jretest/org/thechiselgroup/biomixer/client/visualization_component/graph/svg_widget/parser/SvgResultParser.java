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
package org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.parser;

import org.thechiselgroup.biomixer.client.services.AbstractXMLResultParser;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.text_renderer.TextSvgElement;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.DocumentProcessor;
import org.w3c.dom.Node;

/**
 * Returns the portion of an SVG string specified by an xpath expression. This
 * includes any child elements.
 * 
 * The string returned is a complete SVG file (has an outer SVG element wrapped
 * around it with the xmlns). This allows the selection to be rendered properly
 * in a web browser, which in turn allows it to be inspected visually.
 * 
 * @author drusk
 * 
 */
public class SvgResultParser extends AbstractXMLResultParser {

    public SvgResultParser(DocumentProcessor documentProcessor) {
        super(documentProcessor);
    }

    public String extractElementAsString(String svg, String path)
            throws Exception {
        Object root = parseDocument(svg);

        StringBuilder selection = new StringBuilder();
        DomNodeToStringTransformer domNodeToStringTransformer = new DomNodeToStringTransformer();

        for (Object node : getNodes(root, path)) {
            selection.append(domNodeToStringTransformer.transform((Node) node));
        }

        return wrapWithSvgNamespaceContainerElement(selection.toString());
    }

    public String extractElementAsString(SvgElement svgElement, String xpath)
            throws Exception {
        return extractElementAsString(((TextSvgElement) svgElement).toXML(),
                xpath);
    }

    private String wrapWithSvgNamespaceContainerElement(String svgSelection) {
        return "<svg xmlns=\"" + Svg.NAMESPACE + "\" version=\"1.1\">"
                + svgSelection + "</svg>";
    }
}
