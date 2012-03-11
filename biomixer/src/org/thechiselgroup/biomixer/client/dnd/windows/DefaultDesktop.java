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

import static org.thechiselgroup.biomixer.client.core.development.DevelopmentSettings.isInDevelopmentMode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.command.CommandManager;
import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.geometry.SizeInt;
import org.thechiselgroup.biomixer.client.core.ui.ZIndex;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupManagerFactory;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

// TODO wrapper similar to action bar
public class DefaultDesktop extends AbsolutePanel implements Desktop, SizeInt {

    private static final String CSS_DESKTOP = "desktop";

    private PositionManager positionManager;

    private final DesktopWindowManager windowController;

    private List<WindowPanel> windows = new LinkedList<WindowPanel>();

    private Branding branding;

    private PopupManagerFactory popupManagerFactory;

    @Inject
    public DefaultDesktop(CommandManager commandManager, Branding branding,
            PopupManagerFactory popupManagerFactory) {

        addStyleName(CSS_DESKTOP);

        this.branding = branding;
        this.popupManagerFactory = popupManagerFactory;

        positionManager = new PositionManager(this, 7, 13, 10);
        windowController = new DesktopWindowManager(this, commandManager);

        initBranding();
        disableContextMenu();
    }

    private void addWindowInternal(WindowPanel window) {
        windows.add(window);
        window.setZIndex(ZIndex.DESKTOP_WINDOW_BASE + windows.indexOf(window));
    }

    @Override
    public AbsolutePanel asWidget() {
        return this;
    }

    @Override
    public void bringToFront(WindowPanel window) {
        removeWindowInternal(window);
        addWindowInternal(window);
    }

    @Override
    public void clearWindows() {
        List<WindowPanel> windows = new ArrayList<WindowPanel>(getWindows());
        for (WindowPanel w : windows) {
            removeWindow(w);
        }
    }

    private WindowPanel createWindow(String title, boolean titleEditable,
            Widget contentWidget, int x, int y) {

        WindowPanel window = new WindowPanel(popupManagerFactory);

        window.init(windowController, title, titleEditable, contentWidget);

        addWindowInternal(window);
        add(window, x, y);

        return window;
    }

    // TODO what about views // view factories??
    @Override
    public WindowPanel createWindow(WindowContent content) {
        content.init();

        // FIXME better prediction of window size
        Point point = positionManager.getNextLocation(500, 400);

        WindowPanel window = createWindow(content.getLabel(),
                content instanceof ViewWindowContent, content.asWidget(),
                point.getX(), point.getY());

        window.setViewContent(content);

        return window;
    }

    @Override
    public WindowPanel createWindow(WindowContent content, int x, int y,
            int width, int height) {

        content.init();

        WindowPanel window = createWindow(content.getLabel(),
                content instanceof ViewWindowContent, content.asWidget(), x, y);
        window.setPixelSize(width, height);
        window.setViewContent(content);

        return window;
    }

    // TODO extract
    private void disableContextMenu() {
        // disable the context menu if not in dev mode
        if (!isInDevelopmentMode()) {
            sinkEvents(Event.ONCONTEXTMENU);
        }
    }

    @Override
    public int getHeight() {
        return getOffsetHeight();
    }

    @Override
    public int getWidth() {
        return getOffsetWidth();
    }

    @Override
    public List<WindowPanel> getWindows() {
        return windows;
    }

    // TODO refactor: extract
    private void initBranding() {
        Label appTitleLabel = new Label(branding.getApplicationTitle());
        appTitleLabel.addStyleName("branding-app-title");
        add(appTitleLabel);

        Label minorTitleLabel = new Label(branding.getMinorApplicationTitle());
        minorTitleLabel.addStyleName("branding-minor-app-title");
        add(minorTitleLabel);

        Label copyRightLabel = new Label(branding.getCopyright());
        copyRightLabel.addStyleName("branding-copy-right");
        add(copyRightLabel);
    }

    /**
     * Prevents the browsers context menu from appearing on the desktop.
     */
    @Override
    public void onBrowserEvent(Event event) {
        if (DOM.eventGetType(event) == Event.ONCONTEXTMENU) {
            event.preventDefault();
            event.stopPropagation();
        }
    }

    @Override
    public void removeWindow(WindowPanel window) {
        removeWindowInternal(window);
        remove(window);
    }

    private void removeWindowInternal(WindowPanel window) {
        int index = windows.indexOf(window);
        windows.remove(window);

        // lower z-index of previously higher windows
        for (int i = index; i < windows.size(); i++) {
            windows.get(i).setZIndex(ZIndex.DESKTOP_WINDOW_BASE + i);
        }
    }

}