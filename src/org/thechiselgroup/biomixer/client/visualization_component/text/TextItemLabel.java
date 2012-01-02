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
package org.thechiselgroup.biomixer.client.visualization_component.text;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.EventListener;

public interface TextItemLabel {

    void addStyleName(String cssClass);

    Element getElement();

    String getText();

    void registerHandler(EventListener eventListener);

    void removeStyleName(String cssClass);

    void setFontSize(String fontSize);

    void setText(String text);

}