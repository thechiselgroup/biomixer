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
package org.thechiselgroup.biomixer.client.svg;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public final class XmlTestUtils {

    public static void assertXmlEquals(String expectedXml, String actualXml) {
        try {
            assertEquals(normalize(expectedXml), normalize(actualXml));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String format(Element element) {
        StringBuilder sb = new StringBuilder();

        sb.append("<").append(element.getTagName()).append(" ");
        sb.append(formatAttributes(element));
        sb.deleteCharAt(sb.length() - 1);
        sb.append(">");
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item instanceof Element) {
                sb.append("\n");
                sb.append(format((Element) item));
            } else if (item instanceof Text) {
                sb.append(((Text) item).getTextContent().trim());
            }
        }
        sb.append("\n");
        sb.append("</").append(element.getTagName()).append(">");

        return sb.toString();
    }

    private static String formatAttributes(Element element) {
        StringBuilder sb = new StringBuilder();

        List<Attr> attrList = new ArrayList<Attr>();
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            attrList.add((Attr) attributes.item(i));
        }
        Collections.sort(attrList, new Comparator<Attr>() {
            @Override
            public int compare(Attr o1, Attr o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (Attr attr : attrList) {
            sb.append(attr.getName()).append("=\"").append(attr.getValue())
                    .append("\" ");
        }
        return sb.toString();
    }

    private static String normalize(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true);
        Document parse = factory.newDocumentBuilder().parse(
                new ByteArrayInputStream(xml.getBytes()));
        return format(parse.getDocumentElement());
    }

}