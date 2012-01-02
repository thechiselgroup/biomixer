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

import org.thechiselgroup.choosel.core.client.command.CommandManager;

public class DesktopWindowManager extends AbstractWindowManager {

    public DesktopWindowManager(DefaultDesktop desktopPanel,
            CommandManager commandManager) {

        super(desktopPanel, commandManager);

    }

    @Override
    public void bringToFront(WindowPanel window) {
        getDesktopPanel().bringToFront(window);
    }

    @Override
    public void close(WindowPanel window) {
        getDesktopPanel().removeWindow(window);
    }

    protected DefaultDesktop getDesktopPanel() {
        return (DefaultDesktop) getBoundaryPanel();
    }

}