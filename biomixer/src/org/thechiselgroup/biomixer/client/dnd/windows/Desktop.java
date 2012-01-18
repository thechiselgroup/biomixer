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
package org.thechiselgroup.biomixer.client.dnd.windows;

import java.util.List;

import com.google.gwt.user.client.ui.AbsolutePanel;

// TODO use window model instead of window
public interface Desktop {

    AbsolutePanel asWidget();

    void bringToFront(WindowPanel window);

    void clearWindows();

    WindowPanel createWindow(WindowContent content);

    WindowPanel createWindow(WindowContent content, int x, int y, int width,
            int height);

    List<WindowPanel> getWindows();

    void removeWindow(WindowPanel window);

}